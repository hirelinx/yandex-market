package ru.yandex.practicum.market.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.client.PayClient;
import ru.yandex.practicum.market.config.AppProperties;
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
    private final UserService userService;
    private final AppProperties appProperties;

    @Override
    public Mono<CartCheckoutInfo> getCheckoutInfo(BigDecimal cartTotal) {
        return userService.getCurrentUser().flatMap(user -> payClient.getBalance(user.getId().intValue())
                .map(balance -> checkoutInfo(balance, cartTotal))
                .onErrorResume(PayServiceUnavailableException.class,
                        ex -> Mono.just(new CartCheckoutInfo(false, PAY_UNAVAILABLE_MESSAGE, null)))
                .onErrorResume(PaymentRejectedException.class,
                        ex -> Mono.just(checkoutInfoOnPaymentError(ex))));
    }

    @Override
    public Mono<Void> payOrderAmount(BigDecimal amount) {
        var curUserMono = userService.getCurrentUser();
        var sellerUserMono = userService.findByUsername(appProperties.sellerUser().username());

        return Mono.zip(curUserMono, sellerUserMono).flatMap(t -> {
            var fromAccountId = t.getT1().getId().intValue();
            var toAccountId = t.getT2().getId().intValue();

            // pay creates a missing account on balance request, the seller's balance is never requested otherwise
            return Mono.when(payClient.getBalance(fromAccountId), payClient.getBalance(toAccountId))
                    .then(payClient.transfer(fromAccountId, toAccountId, amount));
        });
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
