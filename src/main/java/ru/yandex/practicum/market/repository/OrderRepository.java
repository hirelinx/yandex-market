package ru.yandex.practicum.market.repository;


import lombok.NonNull;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.market.model.Order;

import java.util.List;
import java.util.Set;

public interface OrderRepository extends R2dbcRepository<@NonNull Order, @NonNull Long> {
}
