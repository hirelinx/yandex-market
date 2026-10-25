package ru.yandex.practicum.market.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.dto.CartCheckoutInfo;
import ru.yandex.practicum.market.model.Order;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.repository.OrderRepository;
import ru.yandex.practicum.market.repository.OrdersItemsRepository;
import ru.yandex.practicum.market.repository.UserRepository;
import ru.yandex.practicum.market.service.PaymentService;
import ru.yandex.practicum.market.support.TestEntityFactory;
import ru.yandex.practicum.market.testcontainer.RedisTestContainer;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Testcontainers
@ImportTestcontainers(RedisTestContainer.class)
class EndpointsIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CountedItemRepository countedItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartCountedItemsRepository cartCountedItemsRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrdersItemsRepository ordersItemsRepository;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @MockitoBean
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        when(paymentService.getCheckoutInfo(any()))
                .thenReturn(Mono.just(new CartCheckoutInfo(true, null, BigDecimal.valueOf(100_000))));
        when(paymentService.payOrderAmount(any())).thenReturn(Mono.empty());

        redisConnectionFactory.getConnection().serverCommands().flushAll();
        ordersItemsRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        cartCountedItemsRepository.deleteAll().block();
        countedItemRepository.deleteAll().block();
        itemRepository.deleteAll().block();
    }

    @Test
    void root_redirectsToItems() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "/items");
    }

    @Test
    void getItems_returnsCatalogPage() {
        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItems_anonymous_hidesCartAndOrdersControls() {
        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).doesNotContain("href=\"/items/new\"");
                    assertThat(html).doesNotContain("href=\"/cart/items\"");
                    assertThat(html).doesNotContain("href=\"/orders\"");
                    assertThat(html).doesNotContain("name=\"action\"");
                    assertThat(html).contains("href=\"/login\"");
                });
    }

    @WithMockUser(username = "user1")
    @Test
    void getItems_authenticated_showsCartAndOrdersControls() {
        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("href=\"/items/new\"");
                    assertThat(html).contains("href=\"/cart/items\"");
                    assertThat(html).contains("href=\"/orders\"");
                    assertThat(html).contains("value=\"PLUS\"");
                    assertThat(html).contains("value=\"MINUS\"");
                    assertThat(html).doesNotContain("href=\"/login\"");
                });
    }

    @Test
    void getItem_anonymous_hidesCartAndOrdersControls() {
        var item = itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE)).block();

        webTestClient.get()
                .uri("/items/{id}", item.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).doesNotContain("href=\"/cart/items\"");
                    assertThat(html).doesNotContain("href=\"/orders\"");
                    assertThat(html).doesNotContain("name=\"action\"");
                    assertThat(html).contains("href=\"/login\"");
                });
    }

    @WithMockUser(username = "user1")
    @Test
    void getItem_authenticated_showsCartAndOrdersControls() {
        var item = itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE)).block();

        webTestClient.get()
                .uri("/items/{id}", item.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("href=\"/cart/items\"");
                    assertThat(html).contains("href=\"/orders\"");
                    assertThat(html).contains("value=\"PLUS\"");
                    assertThat(html).doesNotContain("href=\"/login\"");
                });
    }

    @WithMockUser(username = "user1")
    @Test
    void postItems_addsItemToCart() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", item.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        StepVerifier.create(countedItemRepository.findFirstByItemId(item.getId()))
                .expectNextCount(1)
                .verifyComplete();
    }

    @WithMockUser(username = "user1")
    @Test
    void getNewItem_returnsCreateForm() {
        webTestClient.get()
                .uri("/items/new")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItem_returnsItemPage() {
        var item = itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE)).block();

        webTestClient.get()
                .uri("/items/{id}", item.getId())
                .exchange()
                .expectStatus().isOk();
    }

    @WithMockUser(username = "user1")
    @Test
    void getCartItems_returnsCartPage() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @WithMockUser(username = "user1")
    @Test
    void buy_createsOrder() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", item.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");

        StepVerifier.create(orderRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @WithMockUser(username = "user1")
    @Test
    void addToCart_afterOrder_addsItemToCartAgain() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", item.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .body(BodyInserters.empty())
                .exchange()
                .expectStatus().is3xxRedirection();
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy").exchange().expectStatus().is3xxRedirection();

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", item.getId())
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        StepVerifier.create(
                        userRepository.findByUsername("user1")
                                .flatMap(user -> cartRepository.findFirstByUserId(user.getId()))
                                .flatMapMany(cart -> cartCountedItemsRepository.findByCartId(cart.getId()))
                )
                .expectNextCount(1)
                .verifyComplete();
    }

    @WithMockUser(username = "user1")
    @Test
    void getOrders_returnsOrdersPage() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @WithMockUser(username = "user1")
    @Test
    void getOrders_afterSeveralPurchases_listsEveryOrder() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        addItemToCart(item.getId());
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy").exchange().expectStatus().is3xxRedirection();
        addItemToCart(item.getId());
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy").exchange().expectStatus().is3xxRedirection();

        var orderIds = orderRepository.findAll().map(order -> order.getId()).collectList().block();
        assertThat(orderIds).hasSize(2);

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("Заказ №" + orderIds.get(0));
                    assertThat(html).contains("Заказ №" + orderIds.get(1));
                });
    }

    @WithMockUser(username = "user1")
    @Test
    void getOrders_listsOnlyCurrentUserOrders() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        addItemToCart(item.getId());
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy").exchange().expectStatus().is3xxRedirection();
        var ownOrderId = orderRepository.findAll().single().block().getId();
        var foreignOrderId = saveOrderOf("user2");

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("href=\"/orders/" + ownOrderId + "\"");
                    assertThat(html).doesNotContain("href=\"/orders/" + foreignOrderId + "\"");
                });
    }

    @WithMockUser(username = "user1")
    @Test
    void getOrder_ofAnotherUser_returnsNotFound() {
        var foreignOrderId = saveOrderOf("user2");

        webTestClient.get()
                .uri("/orders/{id}", foreignOrderId)
                .exchange()
                .expectStatus().isNotFound();
    }

    private long saveOrderOf(String username) {
        var user = userRepository.findByUsername(username).block();
        var order = new Order();
        order.setUserId(user.getId());
        return orderRepository.save(order).block().getId();
    }

    private void addItemToCart(long itemId) {
        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", itemId)
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void getFile_returnsUploadedContent() throws Exception {
        Path uploadsDir = Path.of(System.getProperty("java.io.tmpdir"), "market-test-uploads");
        Files.createDirectories(uploadsDir);
        Files.writeString(uploadsDir.resolve("integration.jpg"), "stored-image");

        webTestClient.get()
                .uri("/files/integration.jpg")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("stored-image");
    }
}
