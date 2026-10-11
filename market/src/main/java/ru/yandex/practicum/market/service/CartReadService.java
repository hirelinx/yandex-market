package ru.yandex.practicum.market.service;

import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;

import java.util.List;

public interface CartReadService {
    Flux<@NonNull CountedItem> getCountedItems(Cart cart);

    Mono<@NonNull Cart> retrieveCartEntity();

    Mono<List<@NonNull ItemDto>> retrieveItems(Cart cart);

    void cacheEvictCartItems();
}
