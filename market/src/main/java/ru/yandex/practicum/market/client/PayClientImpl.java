package ru.yandex.practicum.market.client;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.client.dto.PayBalanceResponse;
import ru.yandex.practicum.market.client.dto.PayTransferRequest;
import ru.yandex.practicum.market.exception.PayServiceUnavailableException;
import ru.yandex.practicum.market.exception.PaymentRejectedException;

import java.math.BigDecimal;
import java.time.Duration;

@Component
@AllArgsConstructor
public class PayClientImpl implements PayClient {

    private final WebClient payWebClient;

    @Override
    public Mono<BigDecimal> getBalance(int accountId) {
        return payWebClient.get()
                .uri("/accounts/{accountId}/balance", accountId)
                .retrieve()
                .bodyToMono(PayBalanceResponse.class)
                .map(PayBalanceResponse::balance)
                .timeout(Duration.ofSeconds(3))
                .onErrorMap(WebClientRequestException.class,
                        ex -> new PayServiceUnavailableException("Payment service is unavailable", ex))
                .onErrorMap(WebClientResponseException.class, this::mapBalanceError);
    }

    @Override
    public Mono<Void> transfer(int fromAccountId, int toAccountId, BigDecimal amount) {
        return payWebClient.post()
                .uri("/transfers")
                .bodyValue(new PayTransferRequest(fromAccountId, toAccountId, amount))
                .retrieve()
                .toBodilessEntity()
                .then()
                .timeout(Duration.ofSeconds(3))
                .onErrorMap(WebClientRequestException.class,
                        ex -> new PayServiceUnavailableException("Payment service is unavailable", ex))
                .onErrorMap(WebClientResponseException.class, this::mapResponseError);
    }

    private RuntimeException mapBalanceError(WebClientResponseException ex) {
        if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
            return new PayServiceUnavailableException("Payment service is unavailable", ex);
        }
        return mapTransferError(ex);
    }

    private RuntimeException mapTransferError(WebClientResponseException ex) {
        if (ex.getStatusCode().value() == HttpStatus.UNPROCESSABLE_CONTENT.value()) {
            return new PaymentRejectedException("Not enough money on account");
        }
        if (ex.getStatusCode().is4xxClientError()) {
            return new PaymentRejectedException("Payment was rejected");
        }
        return new PayServiceUnavailableException("Payment service is unavailable", ex);
    }

    private RuntimeException mapResponseError(WebClientResponseException ex) {
        return mapTransferError(ex);
    }
}
