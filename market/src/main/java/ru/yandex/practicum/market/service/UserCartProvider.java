package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.repository.CartRepository;

@Component
@AllArgsConstructor
public class UserCartProvider {
    private final CartRepository cartRepository;

    @Cacheable(cacheNames = "cart", key = "#userId", unless = "#result == null")
    public Mono<@NonNull Cart> getOrCreate(long userId) {
        return cartRepository.findFirstByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    var cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                }));
    }
}