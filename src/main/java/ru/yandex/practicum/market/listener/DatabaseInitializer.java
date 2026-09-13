package ru.yandex.practicum.market.listener;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.repository.CartRepository;

@Component
@AllArgsConstructor
public class DatabaseInitializer {
    private final CartRepository cartRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReadyEvent() {
        var carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            var cart = new Cart();
            cartRepository.save(cart);
        }
    }
}
