package ru.yandex.practicum.market.service;

import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.model.Cart;

import java.util.List;

public interface CartService {
    boolean addToCart(long itemId);

    boolean removeFromCart(long itemId);

    Cart retrieveCartEntity();

    List<ItemDto> retrieveItems();
}
