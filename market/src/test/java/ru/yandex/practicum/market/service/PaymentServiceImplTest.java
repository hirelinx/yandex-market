package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.client.PayClient;
import ru.yandex.practicum.market.config.AppProperties;
import ru.yandex.practicum.market.exception.PayServiceUnavailableException;
import ru.yandex.practicum.market.exception.PaymentRejectedException;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PayClient payClient;

    @Mock
    private UserService userService;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        var appProperties = new AppProperties(
                null,
                List.of(),
                new AppProperties.UserProperties("seller", "neverlogin")
        );
        paymentService = new PaymentServiceImpl(payClient, userService, appProperties);
    }

    @Test
    void getCheckoutInfo_allowsCheckoutWhenBalanceCoversTotal() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(TestEntityFactory.user(1L, "customer", "neverlogin")));
        when(payClient.getBalance(1)).thenReturn(Mono.just(BigDecimal.valueOf(500)));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.valueOf(100)))
                .expectNextMatches(info -> info.canCheckout() && info.balance().compareTo(BigDecimal.valueOf(500)) == 0)
                .verifyComplete();
    }

    @Test
    void getCheckoutInfo_disablesCheckoutWhenBalanceTooLow() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(TestEntityFactory.user(1L, "customer", "neverlogin")));
        when(payClient.getBalance(1)).thenReturn(Mono.just(BigDecimal.TEN));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.valueOf(100)))
                .expectNextMatches(info -> !info.canCheckout() && info.message() != null)
                .verifyComplete();
    }

    @Test
    void getCheckoutInfo_disablesCheckoutWhenPayUnavailable() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(TestEntityFactory.user(1L, "customer", "neverlogin")));
        when(payClient.getBalance(1)).thenReturn(Mono.error(new PayServiceUnavailableException("down")));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.TEN))
                .expectNextMatches(info -> !info.canCheckout() && info.message().contains("недоступен"))
                .verifyComplete();
    }

    @Test
    void getCheckoutInfo_disablesCheckoutWhenBalanceRequestRejected() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(TestEntityFactory.user(1L, "customer", "neverlogin")));
        when(payClient.getBalance(1)).thenReturn(Mono.error(new PaymentRejectedException("Payment was rejected")));

        StepVerifier.create(paymentService.getCheckoutInfo(BigDecimal.TEN))
                .expectNextMatches(info -> !info.canCheckout() && info.message().contains("недоступен"))
                .verifyComplete();
    }

    @Test
    void payOrderAmount_transfersFromCustomerToSeller() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(TestEntityFactory.user(1L, "customer", "neverlogin")));
        when(userService.findByUsername("seller")).thenReturn(Mono.just(TestEntityFactory.user(2L, "seller", "neverlogin")));
        when(payClient.getBalance(1)).thenReturn(Mono.just(BigDecimal.valueOf(1000)));
        when(payClient.getBalance(2)).thenReturn(Mono.just(BigDecimal.valueOf(1000)));
        when(payClient.transfer(1, 2, BigDecimal.TEN)).thenReturn(Mono.empty());

        StepVerifier.create(paymentService.payOrderAmount(BigDecimal.TEN))
                .verifyComplete();

        var inOrder = inOrder(payClient);
        inOrder.verify(payClient).getBalance(2);
        inOrder.verify(payClient).transfer(1, 2, BigDecimal.TEN);
    }

    @Test
    void payOrderAmount_failsWhenPayUnavailable() {
        when(userService.getCurrentUser()).thenReturn(Mono.just(TestEntityFactory.user(1L, "customer", "neverlogin")));
        when(userService.findByUsername("seller")).thenReturn(Mono.just(TestEntityFactory.user(2L, "seller", "neverlogin")));
        when(payClient.getBalance(1)).thenReturn(Mono.just(BigDecimal.valueOf(1000)));
        when(payClient.getBalance(2)).thenReturn(Mono.error(new PayServiceUnavailableException("down")));
        when(payClient.transfer(1, 2, BigDecimal.TEN)).thenReturn(Mono.empty());

        StepVerifier.create(paymentService.payOrderAmount(BigDecimal.TEN))
                .expectError(PayServiceUnavailableException.class)
                .verify();
    }
}
