package ru.yandex.practicum.market.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.User;

public interface UserService {
    Mono<User> findByUsername(String username);

    Mono<User> getCurrentUser();
}
