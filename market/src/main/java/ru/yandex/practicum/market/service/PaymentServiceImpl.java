package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.client.PayClient;
import ru.yandex.practicum.market.config.PayProperties;
import ru.yandex.practicum.market.dto.CartCheckoutInfo;
import ru.yandex.practicum.market.exception.PayServiceUnavailableException;
import ru.yandex.practicum.market.exception.PaymentRejectedException;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String INSUFFICIENT_FUNDS_MESSAGE = "Недостаточно средств на счёте";
    private static final String PAY_UNAVAILABLE_MESSAGE = "Сервис платежей недоступен";

    private final PayClient payClient;
    private final PayProperties payProperties;

    @Override
    public Mono<CartCheckoutInfo> getCheckoutInfo(BigDecimal cartTotal) {
        return payClient.getBalance(payProperties.customerAccountId())
                .map(balance -> checkoutInfo(balance, cartTotal))
                .onErrorResume(PayServiceUnavailableException.class,
                        ex -> Mono.just(new CartCheckoutInfo(false, PAY_UNAVAILABLE_MESSAGE, null)))
                .onErrorResume(PaymentRejectedException.class,
                        ex -> Mono.just(checkoutInfoOnPaymentError(ex)));
    }

    @Override
    public Mono<Void> payOrderAmount(BigDecimal amount) {
        return payClient.transfer(
                payProperties.customerAccountId(),
                payProperties.sellerAccountId(),
                amount
        );
    }

    private CartCheckoutInfo checkoutInfo(BigDecimal balance, BigDecimal cartTotal) {
        if (balance.compareTo(cartTotal) < 0) {
            return new CartCheckoutInfo(false, INSUFFICIENT_FUNDS_MESSAGE, balance);
        }
        return new CartCheckoutInfo(true, null, balance);
    }

    private CartCheckoutInfo checkoutInfoOnPaymentError(PaymentRejectedException ex) {
        if ("Not enough money on account".equals(ex.getMessage())) {
            return new CartCheckoutInfo(false, INSUFFICIENT_FUNDS_MESSAGE, null);
        }
        return new CartCheckoutInfo(false, PAY_UNAVAILABLE_MESSAGE, null);
    }
}
