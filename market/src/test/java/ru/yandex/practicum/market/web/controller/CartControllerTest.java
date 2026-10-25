package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.config.WebFluxSecurityTestConfig;
import ru.yandex.practicum.market.dto.CartCheckoutInfo;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
@Import(WebFluxSecurityTestConfig.class)
class CartControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private CartService cartService;
    @MockitoBean
    private PaymentService paymentService;

    @WithMockUser(username = "user")
    @Test
    void getCartItems_returnsCartView() {
        var cart = new Cart();
        cart.setId(1L);
        when(cartService.retrieveCartEntity()).thenReturn(Mono.just(cart));
        when(cartService.retrieveItems(any(Cart.class)))
                .thenReturn(Mono.just(List.of(new ItemDto(1L, "Молоко", "desc", "files/a.jpg", BigDecimal.TEN, 1))));
        when(paymentService.getCheckoutInfo(any()))
                .thenReturn(Mono.just(new CartCheckoutInfo(true, null, BigDecimal.valueOf(100))));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @WithMockUser(username = "user")
    @Test
    void postCartItems_plusAction_returnsCartView() {
        var cart = new Cart();
        cart.setId(1L);
        when(cartService.addToCart(2L)).thenReturn(Mono.just(true));
        when(cartService.retrieveCartEntity()).thenReturn(Mono.just(cart));
        when(cartService.retrieveItems(any(Cart.class)))
                .thenReturn(Mono.just(List.of(new ItemDto(2L, "Хлеб", "desc", "files/a.jpg", BigDecimal.ONE, 1))));
        when(paymentService.getCheckoutInfo(any()))
                .thenReturn(Mono.just(new CartCheckoutInfo(true, null, BigDecimal.valueOf(100))));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri(uriBuilder -> uriBuilder.path("/cart/items")
                        .queryParam("id", 2)
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).addToCart(2L);
    }
}
