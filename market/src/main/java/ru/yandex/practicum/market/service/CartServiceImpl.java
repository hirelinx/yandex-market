package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;

import java.util.List;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartReadService cartReadService;
    private final CartWriteService cartWriteService;

    @Override
    public Flux<@NonNull CountedItem> getCountedItems(Cart cart) {
        return cartReadService.getCountedItems(cart);
    }

    @Override
    public Mono<@NonNull Cart> retrieveCartEntity() {
        return cartReadService.retrieveCartEntity();
    }

    @Override
    public Mono<List<@NonNull ItemDto>> retrieveItems(Cart cart) {
        return cartReadService.retrieveItems(cart);
    }

    @Override
    public Flux<@NonNull ItemDto> retrieveItems(Mono<Cart> cart) {
        return cart.flatMapMany(c ->
                cartReadService.retrieveItems(c).flatMapMany(Flux::fromIterable)
        );
    }

    @Override
    public void cacheEvictCartItems() {
        cartReadService.cacheEvictCartItems();
    }

    @Override
    public Mono<@NonNull Boolean> addToCart(long itemId) {
        return cartWriteService.addToCart(itemId);
    }

    @Override
    public Mono<@NonNull Boolean> removeFromCart(long itemId) {
        return cartWriteService.removeFromCart(itemId);
    }
}
