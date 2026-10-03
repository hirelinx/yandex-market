package ru.yandex.practicum.market.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Table("item")
public non-sealed class Item implements IEntity{
    @Id
    @Column("id")
    protected Long id;
    @Column("title")
    private String title;
    @Column("description")
    private String description;
    @Column("img_path")
    private String imgPath;
    @Column("price")
    private BigDecimal price;

}
