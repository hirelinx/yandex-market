package ru.yandex.practicum.pay.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.dto.GetBalance200Response;
import ru.yandex.practicum.pay.exception.AccountNotFoundException;
import ru.yandex.practicum.pay.service.AccountsService;

import java.math.BigDecimal;

@Controller
@AllArgsConstructor
public class AccountsController implements AccountsApi{
    private final AccountsService accountsService;

    @Override
    public Mono<ResponseEntity<GetBalance200Response>> getBalance(Integer accountId, ServerWebExchange exchange) {
        Mono<BigDecimal> balance = accountsService.getBalance(accountId);

        return balance
                .map(it -> ResponseEntity.ok().body(new GetBalance200Response().balance(it)))
                .onErrorResume(AccountNotFoundException.class, err ->
                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
                );
    }
}
