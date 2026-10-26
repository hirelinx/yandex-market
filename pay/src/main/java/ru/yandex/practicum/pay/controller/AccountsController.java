package ru.yandex.practicum.pay.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.dto.GetBalance200Response;
import ru.yandex.practicum.pay.service.AccountsService;

@Controller
@AllArgsConstructor
public class AccountsController implements AccountsApi{
    private final AccountsService accountsService;

    @Override
    public Mono<ResponseEntity<GetBalance200Response>> getBalance(Integer accountId, ServerWebExchange exchange) {
        return accountsService.getBalance(accountId)
                .map(it -> ResponseEntity.ok().body(new GetBalance200Response().balance(it)));
    }
}
