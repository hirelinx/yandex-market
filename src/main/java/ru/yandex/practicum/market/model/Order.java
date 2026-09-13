package ru.yandex.practicum.market.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.market.common.HibernateIdentifiable;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Table(name = "orders")
@NamedEntityGraph(name = "Order.items", attributeNodes = @NamedAttributeNode("items"))
public non-sealed class Order extends HibernateIdentifiable<Long> implements IEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToMany
    @ToString.Exclude
    private Set<CountedItem> items =  new HashSet<>();

    @Override
    public Long getIdentity() {
        return getId();
    }
}