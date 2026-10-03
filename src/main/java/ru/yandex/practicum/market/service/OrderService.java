package ru.yandex.practicum.market.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;

public interface OrderService {
    Flux<OrderDto> retrieveOrders();

    Mono<OrderDto> retrieveById(long id);

    Mono<OrderDto> createFromCart();
}
