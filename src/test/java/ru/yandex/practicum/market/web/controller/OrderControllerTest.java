package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private OrderService orderService;

    @Test
    void getOrders_returnsOrdersView() {
        when(orderService.retrieveOrders())
                .thenReturn(Flux.just(new OrderDto(1L, List.of(), BigDecimal.ZERO)));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrders_rendersAllOrdersFromFlux() {
        when(orderService.retrieveOrders()).thenReturn(Flux.just(
                new OrderDto(1L, List.of(), BigDecimal.ZERO),
                new OrderDto(2L, List.of(), BigDecimal.TEN)
        ));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(html -> {
                    assertThat(html).contains("Заказ №1");
                    assertThat(html).contains("Заказ №2");
                });
    }

    @Test
    void getOrder_returnsOrderView() {
        when(orderService.retrieveById(5L))
                .thenReturn(Mono.just(new OrderDto(5L, List.of(), BigDecimal.TEN)));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/orders/5").queryParam("newOrder", true).build())
                .exchange()
                .expectStatus().isOk();
    }
}
