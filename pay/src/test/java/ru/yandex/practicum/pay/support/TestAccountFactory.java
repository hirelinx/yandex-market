package ru.yandex.practicum.pay.support;

import ru.yandex.practicum.pay.model.Account;

import java.math.BigDecimal;

public final class TestAccountFactory {

    private TestAccountFactory() {
    }

    public static Account account(BigDecimal balance) {
        return new Account(balance);
    }

    public static Account account(double balance) {
        return new Account(BigDecimal.valueOf(balance));
    }
}
