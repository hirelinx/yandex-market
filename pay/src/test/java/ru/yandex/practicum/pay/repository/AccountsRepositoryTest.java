package ru.yandex.practicum.pay.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.yandex.practicum.pay.support.TestAccountFactory;

import java.math.BigDecimal;

@DataR2dbcTest
@ActiveProfiles("test")
class AccountsRepositoryTest {

    @Autowired
    private AccountsRepository accountsRepository;

    @Test
    void saveAndFindById() {
        StepVerifier.create(
                        accountsRepository.save(TestAccountFactory.account(100))
                                .flatMap(saved -> accountsRepository.findById(saved.getId()))
                )
                .expectNextMatches(found -> found.getBalance().compareTo(BigDecimal.valueOf(100)) == 0)
                .verifyComplete();
    }

    @Test
    void insert_createsAccountWithGivenId() {
        StepVerifier.create(
                        accountsRepository.deleteById(777_777)
                                .then(accountsRepository.insert(777_777, BigDecimal.valueOf(1000)))
                                .then(accountsRepository.findById(777_777))
                )
                .expectNextMatches(found -> found.getId() == 777_777
                        && found.getBalance().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();
    }

    @Test
    void findById_returnsEmptyWhenMissing() {
        StepVerifier.create(accountsRepository.findById(999_999))
                .verifyComplete();
    }
}
