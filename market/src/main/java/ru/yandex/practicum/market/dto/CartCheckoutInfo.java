package ru.yandex.practicum.market.dto;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;

public record CartCheckoutInfo(
        boolean canCheckout,
        @Nullable String message,
        @Nullable BigDecimal balance
) {
}
