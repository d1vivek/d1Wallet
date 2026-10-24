package com.d1vivek.wallet.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.d1vivek.wallet.domain.error.AccountNotFoundException;
import com.d1vivek.wallet.domain.error.DataUnavailableException;
import com.d1vivek.wallet.domain.error.NotSignedInException;
import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.AccountStatement;
import com.d1vivek.wallet.domain.model.Transaction;
import com.d1vivek.wallet.domain.model.TransactionType;
import com.d1vivek.wallet.testing.FakeAuthRepository;
import com.d1vivek.wallet.testing.FakeWalletRepository;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class GetAccountStatementUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");
    private static final Instant WINDOW_START = NOW.minus(Duration.ofDays(30));
    private static final Currency USD = Currency.getInstance("USD");
    private static final String ACCOUNT_ID = "acc-usd";

    private FakeWalletRepository walletRepository;
    private FakeAuthRepository authRepository;
    private GetAccountStatementUseCase useCase;

    @Before
    public void setUp() {
        walletRepository = new FakeWalletRepository();
        walletRepository.accounts.add(new Account(ACCOUNT_ID, "USD Account", USD, new BigDecimal("100.00")));
        authRepository = FakeAuthRepository.signedIn();
        useCase = new GetAccountStatementUseCase(
                walletRepository, authRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    public void includesTransactionsInsideTheWindowAndDropsOlderOnes() throws Exception {
        addTransaction("t1", NOW.minus(Duration.ofDays(2)));
        addTransaction("t2", NOW.minus(Duration.ofDays(29)));
        addTransaction("t3", NOW.minus(Duration.ofDays(31)));
        addTransaction("t4", NOW.minus(Duration.ofDays(90)));

        assertEquals(List.of("t1", "t2"), idsOf(useCase.execute(ACCOUNT_ID)));
    }

    @Test
    public void transactionExactlyThirtyDaysAgoIsIncluded() throws Exception {
        addTransaction("edge", WINDOW_START);

        assertEquals(List.of("edge"), idsOf(useCase.execute(ACCOUNT_ID)));
    }

    @Test
    public void transactionOneMillisecondBeforeTheWindowIsExcluded() throws Exception {
        addTransaction("too-old", WINDOW_START.minusMillis(1));

        assertTrue(useCase.execute(ACCOUNT_ID).isEmpty());
    }

    @Test
    public void transactionAtExactlyNowIsIncluded() throws Exception {
        addTransaction("now", NOW);

        assertEquals(List.of("now"), idsOf(useCase.execute(ACCOUNT_ID)));
    }

    @Test
    public void futureDatedTransactionsAreExcluded() throws Exception {
        addTransaction("future", NOW.plusMillis(1));
        addTransaction("next-week", NOW.plus(Duration.ofDays(7)));

        assertTrue(useCase.execute(ACCOUNT_ID).isEmpty());
    }

    @Test
    public void transactionsAreOrderedNewestFirst() throws Exception {
        addTransaction("middle", NOW.minus(Duration.ofDays(10)));
        addTransaction("newest", NOW.minus(Duration.ofHours(1)));
        addTransaction("oldest", NOW.minus(Duration.ofDays(20)));

        assertEquals(List.of("newest", "middle", "oldest"), idsOf(useCase.execute(ACCOUNT_ID)));
    }

    @Test
    public void equalTimestampsAreOrderedByIdDescendingRegardlessOfInputOrder() throws Exception {
        Instant sameMoment = NOW.minus(Duration.ofDays(3));
        addTransaction("txn-002", sameMoment);
        addTransaction("txn-003", sameMoment);
        addTransaction("txn-001", sameMoment);

        assertEquals(List.of("txn-003", "txn-002", "txn-001"), idsOf(useCase.execute(ACCOUNT_ID)));
    }

    @Test
    public void transactionsFromOtherAccountsAreIgnored() throws Exception {
        addTransaction("mine", NOW.minus(Duration.ofDays(1)));
        walletRepository.transactions.add(new Transaction("theirs", "acc-other", NOW.minus(Duration.ofDays(1)),
                "Other", TransactionType.DEPOSIT, new BigDecimal("1.00"), new BigDecimal("1.00"), USD));

        assertEquals(List.of("mine"), idsOf(useCase.execute(ACCOUNT_ID)));
    }

    @Test
    public void statementCarriesTheAccountAndTheExactWindow() throws Exception {
        AccountStatement statement = useCase.execute(ACCOUNT_ID);

        assertEquals(ACCOUNT_ID, statement.account().getId());
        assertEquals(new BigDecimal("100.00"), statement.account().getBalance());
        assertEquals(WINDOW_START, statement.from());
        assertEquals(NOW, statement.to());
    }

    @Test
    public void accountWithNoRecentTransactionsGivesAnEmptyStatement() throws Exception {
        AccountStatement statement = useCase.execute(ACCOUNT_ID);

        assertTrue(statement.isEmpty());
        assertEquals(ACCOUNT_ID, statement.account().getId());
    }

    @Test
    public void unknownAccountIdIsReportedAsNotFound() {
        assertThrows(AccountNotFoundException.class, () -> useCase.execute("acc-missing"));
    }

    @Test
    public void missingAccountIdIsReportedAsNotFound() {
        assertThrows(AccountNotFoundException.class, () -> useCase.execute(null));
        assertThrows(AccountNotFoundException.class, () -> useCase.execute(""));
    }

    @Test
    public void repositoryFailureIsPassedThrough() {
        walletRepository.failing = true;

        assertThrows(DataUnavailableException.class, () -> useCase.execute(ACCOUNT_ID));
    }

    @Test
    public void refusesToLoadAfterSignOut() {
        authRepository.signOut();

        assertThrows(NotSignedInException.class, () -> useCase.execute(ACCOUNT_ID));
    }

    private void addTransaction(String id, Instant timestamp) {
        walletRepository.transactions.add(new Transaction(id, ACCOUNT_ID, timestamp, "Test " + id,
                TransactionType.PAYMENT, new BigDecimal("-1.00"), new BigDecimal("99.00"), USD));
    }

    private static List<String> idsOf(AccountStatement statement) {
        List<String> ids = new ArrayList<>();
        for (Transaction transaction : statement.transactions()) {
            ids.add(transaction.getId());
        }
        return ids;
    }
}
