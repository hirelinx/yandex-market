package ru.yandex.practicum.market.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "cart_item")
@NoArgsConstructor
@ToString
public non-sealed class CountedItem implements IEntity {
    @Id
    @Column("id")
    private Long id;

    @NonNull
    @Column("item_id")
    private Long itemId;

    @Column("count")
    private Long count = 1L;

    public CountedItem(@NonNull Long itemId) {
        this.itemId = itemId;
    }
}