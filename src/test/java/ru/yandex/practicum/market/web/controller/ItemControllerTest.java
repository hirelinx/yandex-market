package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.service.ItemService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private CartService cartService;

    @Test
    void getItems_returnsItemsView() {
        when(itemService.search(any(), any()))
                .thenReturn(Mono.just(new ItemsAndPagingDto(List.of(), false, false)));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postItems_plusAction_redirectsToItems() {
        when(cartService.addToCart(1L)).thenReturn(Mono.just(true));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("id", 1)
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", 1)
                        .queryParam("pageSize", 5)
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=&sort=NO&pageNumber=1&pageSize=5");

        verify(cartService).addToCart(1L);
    }

    @Test
    void getNewItem_returnsFormView() {
        webTestClient.get()
                .uri("/items/new")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postNewItem_validRequest_redirectsToItemPage() {
        when(itemService.create(any(), any()))
                .thenReturn(Mono.just(new ItemDto(7L, "Сыр", "desc", "files/a.jpg", BigDecimal.TEN, 0)));

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", "data".getBytes()).filename("photo.jpg").contentType(MediaType.IMAGE_JPEG);
        builder.part("title", "Сыр");
        builder.part("description", "desc");
        builder.part("price", "10.00");

        webTestClient.post()
                .uri("/items/new")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/7");
    }

    @Test
    void getItem_returnsItemView() {
        when(itemService.retrieveById(3L))
                .thenReturn(Mono.just(new ItemDto(3L, "Хлеб", "desc", "files/b.jpg", BigDecimal.ONE, 0)));

        webTestClient.get()
                .uri("/items/3")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postItem_minusAction_returnsItemView() {
        when(cartService.removeFromCart(3L)).thenReturn(Mono.just(true));
        when(itemService.retrieveById(3L))
                .thenReturn(Mono.just(new ItemDto(3L, "Хлеб", "desc", "files/b.jpg", BigDecimal.ONE, 1)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/items/3").queryParam("action", "MINUS").build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).removeFromCart(3L);
    }
}
