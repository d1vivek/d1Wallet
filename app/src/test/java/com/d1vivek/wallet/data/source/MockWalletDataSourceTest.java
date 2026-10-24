package com.d1vivek.wallet.data.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.Transaction;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockWalletDataSourceTest {

    private static final Instant NOW = Instant.parse("2026-10-23T12:00:00Z");
    private final MockWalletDataSource dataSource =
            new MockWalletDataSource(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    public void providesTheChallengeAccountsWithTheirExpectedBalances() {
        assertBalance(MockWalletDataSource.USD_ACCOUNT_ID, "USD", "12450.00");
        assertBalance(MockWalletDataSource.EUR_ACCOUNT_ID, "EUR", "4820.00");
        assertBalance(MockWalletDataSource.SGD_ACCOUNT_ID, "SGD", "8250.00");
        assertBalance(MockWalletDataSource.NPR_ACCOUNT_ID, "NPR", "58000.00");
    }

    @Test
    public void eachBalanceAfterIsThePreviousBalancePlusTheAmount() {
        for (Account account : dataSource.getAccounts()) {
            List<Transaction> ledger = dataSource.getTransactions(account.getId());
            for (int i = 1; i < ledger.size(); i++) {
                BigDecimal expected = ledger.get(i - 1).getBalanceAfter().add(ledger.get(i).getAmount());
                assertEquals("Ledger break at " + ledger.get(i).getId(), expected, ledger.get(i).getBalanceAfter());
            }
        }
    }

    @Test
    public void lastBalanceAfterMatchesTheCurrentAccountBalance() {
        for (Account account : dataSource.getAccounts()) {
            List<Transaction> ledger = dataSource.getTransactions(account.getId());
            assertFalse(ledger.isEmpty());
            assertEquals(account.getBalance(), ledger.get(ledger.size() - 1).getBalanceAfter());
        }
    }

    @Test
    public void transactionsBelongToTheirAccountAndUseItsCurrency() {
        for (Account account : dataSource.getAccounts()) {
            for (Transaction transaction : dataSource.getTransactions(account.getId())) {
                assertEquals(account.getId(), transaction.getAccountId());
                assertEquals(account.getCurrency(), transaction.getCurrency());
            }
        }
    }

    @Test
    public void idsAreUniqueAndTimestampsAreNotInTheFuture() {
        Set<String> ids = new HashSet<>();
        for (Account account : dataSource.getAccounts()) {
            assertTrue(ids.add(account.getId()));
            for (Transaction transaction : dataSource.getTransactions(account.getId())) {
                assertTrue("Duplicate id " + transaction.getId(), ids.add(transaction.getId()));
                assertFalse(transaction.getTimestamp().isAfter(NOW));
            }
        }
    }

    @Test
    public void unknownAccountHasNoTransactions() {
        assertTrue(dataSource.getTransactions("acc-missing").isEmpty());
    }

    @Test
    public void sameClockProducesTheSameData() {
        MockWalletDataSource again = new MockWalletDataSource(Clock.fixed(NOW, ZoneOffset.UTC));

        assertEquals(dataSource.getAccounts(), again.getAccounts());
        assertEquals(dataSource.getTransactions(MockWalletDataSource.USD_ACCOUNT_ID),
                again.getTransactions(MockWalletDataSource.USD_ACCOUNT_ID));
    }

    private void assertBalance(String accountId, String currencyCode, String balance) {
        Account account = dataSource.getAccounts().stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseThrow(AssertionError::new);
        assertEquals(currencyCode, account.getCurrency().getCurrencyCode());
        assertEquals(new BigDecimal(balance), account.getBalance());
    }
}
