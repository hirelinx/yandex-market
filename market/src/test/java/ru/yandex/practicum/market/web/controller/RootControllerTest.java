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
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@WebFluxTest(RootController.class)
@Import(WebFluxSecurityTestConfig.class)
class RootControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private OrderService orderService;

    @Test
    void root_redirectsToItems() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "/items");
    }

    @WithMockUser(username = "user")
    @Test
    void buy_createsOrderAndRedirects() {
        when(orderService.createFromCart())
                .thenReturn(Mono.just(new OrderDto(4L, List.of(), BigDecimal.ZERO)));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/4?newOrder=true");
    }
}
