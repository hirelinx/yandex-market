package ru.yandex.practicum.market.repository;

import lombok.NonNull;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.Order;

public interface OrderRepository extends R2dbcRepository<@NonNull Order, @NonNull Long> {
    Flux<@NonNull Order> findAllByUserId(@NonNull Long userId);

    Mono<@NonNull Order> findByIdAndUserId(@NonNull Long id, @NonNull Long userId);
}
