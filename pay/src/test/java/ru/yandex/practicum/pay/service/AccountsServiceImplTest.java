package ru.yandex.practicum.pay.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.pay.dto.ExecuteTransferRequest;
import ru.yandex.practicum.pay.exception.AccountNotFoundException;
import ru.yandex.practicum.pay.exception.NotEnoughMoneyException;
import ru.yandex.practicum.pay.model.Account;
import ru.yandex.practicum.pay.repository.AccountsRepository;
import ru.yandex.practicum.pay.support.TestAccountFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountsServiceImplTest {

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000);

    @Mock
    private AccountsRepository accountsRepository;

    private AccountsServiceImpl accountsService;

    @BeforeEach
    void setUp() {
        accountsService = new AccountsServiceImpl(accountsRepository, INITIAL_BALANCE);
    }

    @Test
    void getBalance_returnsBalanceWhenAccountExists() {
        Account account = TestAccountFactory.account(BigDecimal.valueOf(250));
        account.setId(1);
        when(accountsRepository.findById(1)).thenReturn(Mono.just(account));

        StepVerifier.create(accountsService.getBalance(1))
                .expectNextMatches(balance -> balance.compareTo(BigDecimal.valueOf(250)) == 0)
                .verifyComplete();
    }

    @Test
    void getBalance_createsAccountWithInitialBalanceWhenMissing() {
        Account created = TestAccountFactory.account(INITIAL_BALANCE);
        created.setId(42);
        when(accountsRepository.findById(42)).thenReturn(Mono.empty(), Mono.just(created));
        when(accountsRepository.insert(42, INITIAL_BALANCE)).thenReturn(Mono.empty());

        StepVerifier.create(accountsService.getBalance(42))
                .expectNextMatches(balance -> balance.compareTo(INITIAL_BALANCE) == 0)
                .verifyComplete();

        verify(accountsRepository).insert(42, INITIAL_BALANCE);
    }

    @Test
    void getBalance_returnsExistingBalanceWhenAccountCreatedConcurrently() {
        Account concurrent = TestAccountFactory.account(BigDecimal.valueOf(900));
        concurrent.setId(42);
        when(accountsRepository.findById(42)).thenReturn(Mono.empty(), Mono.just(concurrent));
        when(accountsRepository.insert(42, INITIAL_BALANCE))
                .thenReturn(Mono.error(new DuplicateKeyException("duplicate id 42")));

        StepVerifier.create(accountsService.getBalance(42))
                .expectNextMatches(balance -> balance.compareTo(BigDecimal.valueOf(900)) == 0)
                .verifyComplete();
    }

    @Test
    void getBalance_doesNotCreateExistingAccount() {
        Account account = TestAccountFactory.account(BigDecimal.valueOf(250));
        account.setId(1);
        when(accountsRepository.findById(1)).thenReturn(Mono.just(account));

        StepVerifier.create(accountsService.getBalance(1))
                .expectNextCount(1)
                .verifyComplete();

        verify(accountsRepository, never()).insert(any(), any());
    }

    @Test
    void transfer_movesAmountBetweenAccounts() {
        Account from = TestAccountFactory.account(1000);
        from.setId(1);
        Account to = TestAccountFactory.account(200);
        to.setId(2);

        when(accountsRepository.findById(1)).thenReturn(Mono.just(from));
        when(accountsRepository.findById(2)).thenReturn(Mono.just(to));
        when(accountsRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        ExecuteTransferRequest request = new ExecuteTransferRequest()
                .from(1)
                .to(2)
                .amount(BigDecimal.valueOf(300));

        StepVerifier.create(accountsService.transfer(request))
                .verifyComplete();

        assertThat(from.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
        assertThat(to.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void transfer_failsWhenSourceAccountMissing() {
        when(accountsRepository.findById(1)).thenReturn(Mono.empty());
        when(accountsRepository.findById(2)).thenReturn(Mono.just(TestAccountFactory.account(100)));

        ExecuteTransferRequest request = new ExecuteTransferRequest()
                .from(1)
                .to(2)
                .amount(BigDecimal.TEN);

        StepVerifier.create(accountsService.transfer(request))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    void transfer_failsWhenDestinationAccountMissing() {
        Account from = TestAccountFactory.account(100);
        from.setId(1);
        when(accountsRepository.findById(1)).thenReturn(Mono.just(from));
        when(accountsRepository.findById(2)).thenReturn(Mono.empty());

        ExecuteTransferRequest request = new ExecuteTransferRequest()
                .from(1)
                .to(2)
                .amount(BigDecimal.TEN);

        StepVerifier.create(accountsService.transfer(request))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    void transfer_failsWhenNotEnoughMoney() {
        Account from = TestAccountFactory.account(50);
        from.setId(1);
        Account to = TestAccountFactory.account(100);
        to.setId(2);

        when(accountsRepository.findById(1)).thenReturn(Mono.just(from));
        when(accountsRepository.findById(2)).thenReturn(Mono.just(to));

        ExecuteTransferRequest request = new ExecuteTransferRequest()
                .from(1)
                .to(2)
                .amount(BigDecimal.valueOf(100));

        StepVerifier.create(accountsService.transfer(request))
                .expectError(NotEnoughMoneyException.class)
                .verify();
    }
}
