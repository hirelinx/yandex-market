package ru.yandex.practicum.pay.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.dto.ExecuteTransferRequest;

import java.math.BigDecimal;

public interface AccountsService {
    Mono<BigDecimal> getBalance(Integer accountId);

    Mono<Void> transfer(ExecuteTransferRequest transferRequest);
}
