package ru.yandex.practicum.market.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.market.model.OrdersItems;

public interface OrdersItemsRepository extends R2dbcRepository<OrdersItems, Long> {
    Flux<OrdersItems> findByOrderId(Long orderId);
    Flux<OrdersItems> findByItemsId(Long itemId);
}
