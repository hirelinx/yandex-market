package ru.yandex.practicum.market.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.dto.CartCheckoutInfo;

import java.math.BigDecimal;

public interface PaymentService {

    Mono<CartCheckoutInfo> getCheckoutInfo(BigDecimal cartTotal);

    Mono<Void> payOrderAmount(BigDecimal amount);
}
