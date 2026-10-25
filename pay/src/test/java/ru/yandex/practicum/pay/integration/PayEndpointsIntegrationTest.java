package ru.yandex.practicum.pay.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;
import ru.yandex.practicum.pay.repository.AccountsRepository;
import ru.yandex.practicum.pay.support.TestAccountFactory;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PayEndpointsIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccountsRepository accountsRepository;

    private Integer fromAccountId;
    private Integer toAccountId;

    @BeforeEach
    void setUp() {
        accountsRepository.deleteAll().block();
        fromAccountId = accountsRepository.save(TestAccountFactory.account(1000)).block().getId();
        toAccountId = accountsRepository.save(TestAccountFactory.account(500)).block().getId();
    }

    @Test
    void getBalance_returnsCurrentBalance() {
        webTestClient.get()
                .uri("/accounts/{id}/balance", fromAccountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(1000);
    }

    @Test
    void getBalance_createsUnknownAccountWithInitialBalance() {
        expectBalance(999_999, BigDecimal.valueOf(1000));

        StepVerifier.create(accountsRepository.findById(999_999))
                .expectNextMatches(account -> account.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();
    }

    @Test
    void executeTransfer_toAccountCreatedByGetBalance_updatesBothBalances() {
        expectBalance(888_888, BigDecimal.valueOf(1000));

        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":%d,"to":888888,"amount":100}
                        """.formatted(fromAccountId))
                .exchange()
                .expectStatus().isCreated();

        expectBalance(fromAccountId, BigDecimal.valueOf(900));
        expectBalance(888_888, BigDecimal.valueOf(1100));
    }

    @Test
    void executeTransfer_updatesBothBalances() {
        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":%d,"to":%d,"amount":250}
                        """.formatted(fromAccountId, toAccountId))
                .exchange()
                .expectStatus().isCreated();

        expectBalance(fromAccountId, BigDecimal.valueOf(750));
        expectBalance(toAccountId, BigDecimal.valueOf(750));
    }

    @Test
    void executeTransfer_returnsNotFoundWhenAccountMissing() {
        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":%d,"to":999999,"amount":1}
                        """.formatted(fromAccountId))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void executeTransfer_returnsUnprocessableWhenNotEnoughMoney() {
        webTestClient.post()
                .uri("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"from":%d,"to":%d,"amount":5000}
                        """.formatted(fromAccountId, toAccountId))
                .exchange()
                .expectStatus().isEqualTo(422);

        expectBalance(fromAccountId, BigDecimal.valueOf(1000));
        expectBalance(toAccountId, BigDecimal.valueOf(500));
    }

    private void expectBalance(Integer accountId, BigDecimal expected) {
        webTestClient.get()
                .uri("/accounts/{id}/balance", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(expected.doubleValue());
    }
}
