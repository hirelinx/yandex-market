package ru.yandex.practicum.market.model;

public sealed interface IEntity permits Cart, CountedItem, Item, Order {
}
