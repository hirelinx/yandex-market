package ru.yandex.practicum.market.repository;


import lombok.NonNull;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.CountedItem;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CountedItemRepository extends R2dbcRepository<@NonNull CountedItem,@NonNull Long> {
    Mono<@NonNull CountedItem> findFirstByItemId(Long id);

    Flux<@NonNull CountedItem> findAllByItemIdIn(List<Long> ids);
    Flux<@NonNull CountedItem> findAllByItemId(Long id);
}
