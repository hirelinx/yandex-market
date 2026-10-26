package ru.yandex.practicum.market.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.User;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUsername(String username);
}
