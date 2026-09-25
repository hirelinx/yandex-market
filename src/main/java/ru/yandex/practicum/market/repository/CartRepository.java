package ru.yandex.practicum.market.repository;

import lombok.NonNull;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.Cart;

@Repository
public interface CartRepository extends R2dbcRepository<@NonNull Cart,@NonNull  Long> {
    Mono<@NonNull Cart> findFirstBy();
}
