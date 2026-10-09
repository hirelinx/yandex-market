package ru.yandex.practicum.market.service;


import lombok.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Cart;

public interface CartService extends CartWriteService, CartReadService {
    Flux<@NonNull ItemDto> retrieveItems(Mono<Cart> cart);
}
