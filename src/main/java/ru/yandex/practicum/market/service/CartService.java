package ru.yandex.practicum.market.service;

import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;

public interface CartService {
    Mono<@NonNull Boolean> addToCart(long itemId);

    Mono<@NonNull Boolean> removeFromCart(long itemId);

    Flux<@NonNull CountedItem> getCountedItems(Cart cart);

    Mono<@NonNull Cart> retrieveCartEntity();

    Flux<@NonNull ItemDto> retrieveItems();
}
