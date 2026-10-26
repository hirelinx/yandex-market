package ru.yandex.practicum.pay.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.pay.model.Account;

import java.math.BigDecimal;

public interface AccountsRepository extends R2dbcRepository<Account, Integer> {
    @Modifying
    @Query("INSERT INTO accounts (id, balance) VALUES (:id, :balance)")
    Mono<Void> insert(Integer id, BigDecimal balance);
}
