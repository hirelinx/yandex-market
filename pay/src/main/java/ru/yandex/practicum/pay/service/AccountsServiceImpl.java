package ru.yandex.practicum.pay.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import ru.yandex.practicum.pay.dto.ExecuteTransferRequest;
import ru.yandex.practicum.pay.exception.AccountNotFoundException;
import ru.yandex.practicum.pay.exception.NotEnoughMoneyException;
import ru.yandex.practicum.pay.model.Account;
import ru.yandex.practicum.pay.repository.AccountsRepository;

import java.math.BigDecimal;

@Service
public class AccountsServiceImpl implements AccountsService {
    private final AccountsRepository accountsRepository;
    private final BigDecimal initialBalance;

    public AccountsServiceImpl(AccountsRepository accountsRepository,
                               @Value("${application.init-balance}") BigDecimal initialBalance) {
        this.accountsRepository = accountsRepository;
        this.initialBalance = initialBalance;
    }

    @Override
    public Mono<BigDecimal> getBalance(Integer accountId) {
        return accountsRepository.findById(accountId)
                .switchIfEmpty(Mono.defer(() -> createAccount(accountId)))
                .map(Account::getBalance);
    }

    private Mono<Account> createAccount(Integer accountId) {
        return accountsRepository.insert(accountId, initialBalance)
                .onErrorResume(DataIntegrityViolationException.class, ex -> Mono.empty())
                .then(Mono.defer(() -> accountsRepository.findById(accountId)));
    }

    @Override
    @Transactional
    public Mono<Void> transfer(ExecuteTransferRequest transferRequest) {
        var from = accountsRepository
                .findById(transferRequest.getFrom())
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found: " + transferRequest.getFrom())));
        var to = accountsRepository
                .findById(transferRequest.getTo())
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found: " + transferRequest.getTo())));

        return Flux.zip(from, to)
                .<Tuple2<Account, Account>>handle((it, sink) -> {
                    if (it.getT1().getBalance().compareTo(transferRequest.getAmount()) < 0) {
                        sink.error(new NotEnoughMoneyException("Not enough money"));
                        return;
                    }
                    sink.next(it);
                })
                .map(it -> {
                    it.getT1().setBalance(it.getT1().getBalance().subtract(transferRequest.getAmount()));
                    it.getT2().setBalance(it.getT2().getBalance().add(transferRequest.getAmount()));
                    return it;
                }).flatMap(it ->
                        accountsRepository.save(it.getT1())
                                .then(accountsRepository.save(it.getT2()))
                ).then();
    }
}
