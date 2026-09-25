package ru.yandex.practicum.market.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Table("cart_counted_items")
public class CartCountedItems {
    @Column("cart_id")
    private Long cartId;
    @Column("counted_items_id")
    private Long countedItemsId;
}
