package ru.yandex.practicum.market.client.dto;

import java.math.BigDecimal;

public record PayTransferRequest(Integer from, Integer to, BigDecimal amount) {
}
