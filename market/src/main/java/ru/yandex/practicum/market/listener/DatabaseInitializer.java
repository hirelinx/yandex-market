package ru.yandex.practicum.market.listener;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.config.AppProperties;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.User;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.UserRepository;


@Component
@AllArgsConstructor
public class DatabaseInitializer {
    private final AppProperties appProperties;
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        // anon user
        registerUser("", "neverlogin").block();
        registerUser(appProperties.sellerUser().username(), appProperties.sellerUser().passwordHash()).block();

        appProperties.users().stream().map(userProperties ->
             registerUser(userProperties.username(), userProperties.passwordHash())
        ).forEach(Mono::block);
    }

    public Mono<User> registerUser(String username, String passwordHash) {
        return userRepository.findByUsername(username).flatMap((user) -> {
            user.setPasswordHash(passwordHash);
            return userRepository.save(user);
        }).switchIfEmpty(Mono.defer(() -> {
            var user = new User();
            user.setUsername(username);
            user.setPasswordHash(passwordHash);
            return userRepository.save(user);
        }));
    }
}
