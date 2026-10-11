package ru.yandex.practicum.pay.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.model.Account;
import ru.yandex.practicum.pay.repository.AccountsRepository;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer {

    public static final int CUSTOMER_ACCOUNT_ID = 1;
    public static final int SELLER_ACCOUNT_ID = 2;

    @Value("${application.init-balance}")
    private BigDecimal initialBalance;
    @NonNull
    private final AccountsRepository accountRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        ensureAccount(CUSTOMER_ACCOUNT_ID, initialBalance).block();
        ensureAccount(SELLER_ACCOUNT_ID, initialBalance).block();
    }

    private Mono<Account> ensureAccount(int id, BigDecimal balance) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    Account account = new Account(balance);
                    account.setId(id);
                    return accountRepository.save(account);
                }));
    }
}
