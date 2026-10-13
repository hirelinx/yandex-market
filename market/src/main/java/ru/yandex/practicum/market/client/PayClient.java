package ru.yandex.practicum.market.client;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PayClient {

    Mono<BigDecimal> getBalance(int accountId);

    Mono<Void> transfer(int fromAccountId, int toAccountId, BigDecimal amount);
}
