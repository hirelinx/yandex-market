package ru.yandex.practicum.market.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "cart")
@ToString
public non-sealed class Cart implements IEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany
    @ToString.Exclude
    private Set<CountedItem> countedItems = new HashSet<>();
}