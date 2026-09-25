package ru.yandex.practicum.market.listener;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.repository.CartRepository;

@Component
@AllArgsConstructor
public class DatabaseInitializer {
    private final CartRepository cartRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        cartRepository.count()
                .filter(count -> count == 0)
                .flatMap(count -> cartRepository.save(new Cart()))
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
}
