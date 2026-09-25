package ru.yandex.practicum.market.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.model.CartCountedItems;

@Repository
public interface CartCountedItemsRepository extends R2dbcRepository<CartCountedItems, Long> {
    Flux<CartCountedItems> findByCartId(Long cartId);
    Flux<CartCountedItems> findByCountedItemsId(Long itemId);

    Mono<Void> deleteCartCountedItemsByCountedItemsId(Long countedItemsId);
}
