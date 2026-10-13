package ru.yandex.practicum.pay.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.dto.ExecuteTransferRequest;
import ru.yandex.practicum.pay.exception.AccountNotFoundException;
import ru.yandex.practicum.pay.exception.NotEnoughMoneyException;
import ru.yandex.practicum.pay.service.AccountsService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(TransfersController.class)
class TransfersControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AccountsService accountsService;

    @Test
    void executeTransfer_returnsOkWhenSuccessful() {
        when(accountsService.transfer(any(ExecuteTransferRequest.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":1,"to":2,"amount":10}
                        """)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void executeTransfer_returnsNotFoundWhenAccountMissing() {
        when(accountsService.transfer(any(ExecuteTransferRequest.class)))
                .thenReturn(Mono.error(new AccountNotFoundException("Account not found")));

        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":1,"to":2,"amount":10}
                        """)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void executeTransfer_returnsUnprocessableWhenNotEnoughMoney() {
        when(accountsService.transfer(any(ExecuteTransferRequest.class)))
                .thenReturn(Mono.error(new NotEnoughMoneyException("Not enough money")));

        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":1,"to":2,"amount":10000}
                        """)
                .exchange()
                .expectStatus().isEqualTo(422);
    }
}
