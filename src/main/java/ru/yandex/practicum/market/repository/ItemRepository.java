package ru.yandex.practicum.market.repository;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.market.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends R2dbcRepository<@NonNull Item, @NonNull Long> {
    Flux<@NonNull Item> findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);
}
