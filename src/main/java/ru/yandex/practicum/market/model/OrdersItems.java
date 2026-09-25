package ru.yandex.practicum.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@AllArgsConstructor
@Table("orders_items")
public class OrdersItems {
    @Column("order_id")
    private Long orderId;
    @Column("items_id")
    private Long itemsId;
}
