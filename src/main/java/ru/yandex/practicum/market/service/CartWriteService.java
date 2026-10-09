package ru.yandex.practicum.market.service;

import lombok.NonNull;
import reactor.core.publisher.Mono;

public interface CartWriteService {
    Mono<@NonNull Boolean> addToCart(long itemId);

    Mono<@NonNull Boolean> removeFromCart(long itemId);
}
