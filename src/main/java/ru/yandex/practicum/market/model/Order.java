package ru.yandex.practicum.market.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "orders")
public non-sealed class Order implements IEntity {
    @Id
    @Column("id")
    private Long id;
}