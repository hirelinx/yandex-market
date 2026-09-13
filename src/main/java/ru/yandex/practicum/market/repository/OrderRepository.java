package ru.yandex.practicum.market.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.market.model.Order;

import java.util.List;
import java.util.Set;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph("Order.items")
    Set<Order> findAllBy();
}
