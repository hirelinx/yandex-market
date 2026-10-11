package ru.yandex.practicum.pay.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.pay.model.Account;

public interface AccountsRepository extends R2dbcRepository<Account, Integer> {
}
