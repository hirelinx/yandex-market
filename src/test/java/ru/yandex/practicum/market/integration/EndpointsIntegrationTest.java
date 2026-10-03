package ru.yandex.practicum.market.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.repository.OrderRepository;
import ru.yandex.practicum.market.repository.OrdersItemsRepository;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
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
    private CartCountedItemsRepository cartCountedItemsRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrdersItemsRepository ordersItemsRepository;
    @BeforeEach
    void setUp() {
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
    void postItems_addsItemToCart() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();

        webTestClient.post()
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

    @Test
    void getCartItems_returnsCartPage() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void buy_createsOrder() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        webTestClient.post()
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

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");

        StepVerifier.create(orderRepository.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void addToCart_afterOrder_addsItemToCartAgain() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        webTestClient.post()
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
        webTestClient.post().uri("/buy").exchange().expectStatus().is3xxRedirection();

        webTestClient.post()
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
                        cartRepository.findFirstBy()
                                .flatMapMany(cart -> cartCountedItemsRepository.findByCartId(cart.getId()))
                )
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void getOrders_returnsOrdersPage() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrders_afterSeveralPurchases_listsEveryOrder() {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN)).block();
        addItemToCart(item.getId());
        webTestClient.post().uri("/buy").exchange().expectStatus().is3xxRedirection();
        addItemToCart(item.getId());
        webTestClient.post().uri("/buy").exchange().expectStatus().is3xxRedirection();

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

    private void addItemToCart(long itemId) {
        webTestClient.post()
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
