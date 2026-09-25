package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.service.CartService;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
class CartControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private CartService cartService;

    @Test
    void getCartItems_returnsCartView() {
        when(cartService.retrieveItems())
                .thenReturn(Flux.just(new ItemDto(1L, "Молоко", "desc", "files/a.jpg", BigDecimal.TEN, 1)));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postCartItems_plusAction_returnsCartView() {
        when(cartService.addToCart(2L)).thenReturn(Mono.just(true));
        when(cartService.retrieveItems())
                .thenReturn(Flux.just(new ItemDto(2L, "Хлеб", "desc", "files/a.jpg", BigDecimal.ONE, 1)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/cart/items")
                        .queryParam("id", 2)
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).addToCart(2L);
    }
}
