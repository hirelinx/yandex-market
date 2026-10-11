package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.client.PayClient;
import ru.yandex.practicum.market.config.PayProperties;
import ru.yandex.practicum.market.exception.PayServiceUnavailableException;
import ru.yandex.practicum.market.exception.PaymentRejectedException;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PayClient payClient;

    @Mock
    private PayProperties payProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void getCheckoutInfo_allowsCheckoutWhenBalanceCoversTotal() {
        when(payProperties.customerAccountId()).thenReturn(1);
        when(payClient.getBalance(1)).thenReturn(Mono.just(BigDecimal.valueOf(500)));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.valueOf(100)))
                .expectNextMatches(info -> info.canCheckout() && info.balance().compareTo(BigDecimal.valueOf(500)) == 0)
                .verifyComplete();
    }

    @Test
    void getCheckoutInfo_disablesCheckoutWhenBalanceTooLow() {
        when(payProperties.customerAccountId()).thenReturn(1);
        when(payClient.getBalance(1)).thenReturn(Mono.just(BigDecimal.TEN));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.valueOf(100)))
                .expectNextMatches(info -> !info.canCheckout() && info.message() != null)
                .verifyComplete();
    }

    @Test
    void getCheckoutInfo_disablesCheckoutWhenPayUnavailable() {
        when(payProperties.customerAccountId()).thenReturn(1);
        when(payClient.getBalance(1)).thenReturn(Mono.error(new PayServiceUnavailableException("down")));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.TEN))
                .expectNextMatches(info -> !info.canCheckout() && info.message().contains("недоступен"))
                .verifyComplete();
    }

    @Test
    void getCheckoutInfo_disablesCheckoutWhenBalanceRequestRejected() {
        when(payProperties.customerAccountId()).thenReturn(1);
        when(payClient.getBalance(1)).thenReturn(Mono.error(new PaymentRejectedException("Payment was rejected")));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.TEN))
                .expectNextMatches(info -> !info.canCheckout() && info.message().contains("недоступен"))
                .verifyComplete();
    }

    @Test
    void payOrderAmount_transfersFromCustomerToSeller() {
        when(payProperties.customerAccountId()).thenReturn(1);
        when(payProperties.sellerAccountId()).thenReturn(2);
        when(payClient.transfer(1, 2, BigDecimal.TEN)).thenReturn(Mono.empty());

        StepVerifier.create(paymentService.payOrderAmount(BigDecimal.TEN))
                .verifyComplete();

        verify(payClient).transfer(1, 2, BigDecimal.TEN);
    }
}
