package ru.yandex.practicum.market.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.OrderDto;

public interface OrderWriteService {
    Mono<OrderDto> createFromCart();
}
