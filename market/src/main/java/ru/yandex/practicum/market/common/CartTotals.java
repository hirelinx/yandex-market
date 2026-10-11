package ru.yandex.practicum.market.common;

import ru.yandex.practicum.market.dto.ItemDto;

import java.math.BigDecimal;
import java.util.List;

public final class CartTotals {

    private CartTotals() {
    }

    public static BigDecimal totalOf(List<ItemDto> items) {
        return items.stream()
                .map(it -> it.price().multiply(BigDecimal.valueOf(it.count())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
