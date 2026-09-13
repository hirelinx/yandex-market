package ru.yandex.practicum.market.integration;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.repository.OrderRepository;
import ru.yandex.practicum.market.service.FilesService;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndpointsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CountedItemRepository countedItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private FilesService filesService;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        countedItemRepository.deleteAll();
        itemRepository.deleteAll();

        var carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            cartRepository.save(TestEntityFactory.emptyCart());
        } else {
            var cart = carts.getFirst();

            cart.getCountedItems().clear();
            cartRepository.save(cart);
        }
    }

    @Test
    void root_redirectsToItems() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("items"));
    }

    @Test
    void getItems_returnsCatalogPage() throws Exception {
        itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void postItems_addsItemToCart() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));

        mockMvc.perform(post("/items")
                        .param("id", item.getId().toString())
                        .param("action", "PLUS")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

        assertThat(countedItemRepository.findFirstByItem_Id(item.getId())).isPresent();
    }

    @Test
    void getNewItem_returnsCreateForm() throws Exception {
        mockMvc.perform(get("/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("item-new"));
    }

    @Test
    void postNewItem_createsItemAndRedirects() throws Exception {
        var image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "image-content".getBytes());

        var result = mockMvc.perform(multipart("/items/new")
                        .file(image)
                        .param("title", "Сыр")
                        .param("description", "Описание")
                        .param("price", "150.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/items/*"))
                .andReturn();

        var redirectUrl = result.getResponse().getRedirectedUrl();
        assertThat(redirectUrl).isNotNull();
        var itemId = Long.parseLong(redirectUrl.substring("/items/".length()));
        var savedItem = itemRepository.findById(itemId);
        assertThat(savedItem).isPresent();
        assertThat(savedItem.get().getTitle()).isEqualTo("Сыр");
        assertThat(savedItem.get().getImgPath()).startsWith("files/");
    }

    @Test
    void getItem_returnsItemPage() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE));

        mockMvc.perform(get("/items/{id}", item.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));
    }

    @Test
    void postItem_updatesCartOnItemPage() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Хлеб", BigDecimal.ONE));

        mockMvc.perform(post("/items/{id}", item.getId()).param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        assertThat(countedItemRepository.findFirstByItem_Id(item.getId())).isPresent();
    }

    @Test
    void getCartItems_returnsCartPage() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void postCartItems_modifiesCart() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));

        mockMvc.perform(post("/cart/items")
                        .param("id", item.getId().toString())
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void buy_createsOrder() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));
        mockMvc.perform(post("/items")
                .param("id", item.getId().toString())
                .param("action", "PLUS")
                .param("search", "")
                .param("sort", "NO")
                .param("pageNumber", "1")
                .param("pageSize", "5"));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));

        assertThat(orderRepository.findAll()).hasSize(1);
    }

    @Test
    void addToCart_afterOrder_addsItemToCartAgain() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));
        mockMvc.perform(post("/items")
                .param("id", item.getId().toString())
                .param("action", "PLUS")
                .param("search", "")
                .param("sort", "NO")
                .param("pageNumber", "1")
                .param("pageSize", "5"));
        mockMvc.perform(post("/buy"));

        mockMvc.perform(post("/items")
                        .param("id", item.getId().toString())
                        .param("action", "PLUS")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().is3xxRedirection());

        var cart = cartRepository.findAll().getFirst();
        assertThat(cart.getCountedItems()).hasSize(1);
        assertThat(cart.getCountedItems().iterator().next().getCount()).isEqualTo(1L);
    }

    @Test
    void getOrders_returnsOrdersPage() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void getOrder_returnsOrderPage() throws Exception {
        var item = itemRepository.save(TestEntityFactory.item("Молоко", BigDecimal.TEN));
        mockMvc.perform(post("/items")
                .param("id", item.getId().toString())
                .param("action", "PLUS")
                .param("search", "")
                .param("sort", "NO")
                .param("pageNumber", "1")
                .param("pageSize", "5"));
        var buyResult = mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        var orderId = buyResult.getResponse().getRedirectedUrl()
                .replace("/orders/", "")
                .replace("?newOrder=true", "");

        mockMvc.perform(get("/orders/{id}", orderId).param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"));
    }

    @Test
    void getFile_returnsUploadedContent() throws Exception {
        var image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "stored-image".getBytes());
        filesService.upload(image, "integration.jpg");

        mockMvc.perform(get("/files/integration.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().bytes("stored-image".getBytes()));
    }
}
