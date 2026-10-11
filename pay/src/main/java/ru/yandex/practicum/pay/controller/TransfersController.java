package ru.yandex.practicum.pay.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.dto.ExecuteTransferRequest;
import ru.yandex.practicum.pay.exception.AccountNotFoundException;
import ru.yandex.practicum.pay.exception.NotEnoughMoneyException;
import ru.yandex.practicum.pay.service.AccountsService;

import java.net.URI;

@Controller
@AllArgsConstructor
public class TransfersController implements TransfersApi {
    private final AccountsService accountsService;

    @Override
    public Mono<ResponseEntity<Void>> executeTransfer(Mono<ExecuteTransferRequest> executeTransferRequest,
                                                      ServerWebExchange exchange) {

        return executeTransferRequest
                .flatMap(it -> accountsService.transfer(it))
                .then(executeTransferRequest.map(req ->
                        ResponseEntity.created(URI.create("/accounts/" + req.getFrom() + "/balance"))
                                .<Void>build()))
                .onErrorResume(AccountNotFoundException.class, err ->
                        Mono.just(ResponseEntity.notFound().build())
                ).onErrorResume(NotEnoughMoneyException.class, err ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).build()));
    }
}
