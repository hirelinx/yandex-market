package ru.yandex.practicum.market.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PayBalanceResponse(BigDecimal balance) {
}
