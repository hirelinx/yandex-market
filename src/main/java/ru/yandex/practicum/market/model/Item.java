package ru.yandex.practicum.market.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@ToString
public non-sealed class Item implements IEntity{
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    protected Long id;

    private String title;
    private String description;
    private String imgPath;
    private BigDecimal price;

}
