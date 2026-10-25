package ru.yandex.practicum.pay.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.service.AccountsService;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@WebFluxTest(AccountsController.class)
class AccountsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountsService accountsService;

    @Test
    void getBalance_returnsBalance() {
        when(accountsService.getBalance(7)).thenReturn(Mono.just(BigDecimal.valueOf(123.45)));

        webTestClient.get()
                .uri("/accounts/7/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(123.45);
    }
}
