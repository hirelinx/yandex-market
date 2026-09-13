package ru.yandex.practicum.market.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.market.common.HibernateIdentifiable;

@Getter
@Setter
@Entity
@Table(name = "cart_item")
@NoArgsConstructor
@ToString
public non-sealed class CountedItem extends HibernateIdentifiable<Long> implements IEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item", nullable = false)
    @NonNull
    private Item item;

    @Column(name = "count", nullable = false)
    private Long count = 1L;

    public CountedItem(@NonNull Item item) {
        this.item = item;
    }


    @Override
    public Long getIdentity() {
        return item.getId();
    }
}