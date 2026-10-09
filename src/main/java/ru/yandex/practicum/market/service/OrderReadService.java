package ru.yandex.practicum.market.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;

public interface OrderReadService {
    Flux<OrderDto> retrieveOrders();

    Mono<OrderDto> retrieveById(long id);

    void cacheEvictOrders();

    void cacheEvictOrderById(long id);
}
