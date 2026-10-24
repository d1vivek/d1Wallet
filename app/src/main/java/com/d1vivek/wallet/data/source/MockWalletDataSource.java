package com.d1vivek.wallet.data.source;

import com.d1vivek.wallet.domain.model.Account;
import com.d1vivek.wallet.domain.model.Transaction;
import com.d1vivek.wallet.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Demo accounts and transactions, held in memory.
 * Timestamps are relative to the clock passed in, which keeps the "last 30 days" view populated
 * whenever the app runs and makes tests repeatable.
 * Some entries are deliberately older than 30 days so the statement filter has something to drop.
 * The NPR account only has old entries, which shows the empty statement state.
 */
public final class MockWalletDataSource {

    public static final String USD_ACCOUNT_ID = "acc-usd-001";
    public static final String EUR_ACCOUNT_ID = "acc-eur-001";
    public static final String SGD_ACCOUNT_ID = "acc-sgd-001";
    public static final String NPR_ACCOUNT_ID = "acc-npr-001";

    private final List<Account> accounts;
    private final Map<String, List<Transaction>> transactionsByAccount;

    public MockWalletDataSource(Clock clock) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MINUTES);
        List<Account> accountList = new ArrayList<>();
        Map<String, List<Transaction>> ledgers = new LinkedHashMap<>();

        Ledger usd = new Ledger(USD_ACCOUNT_ID, "USD", "9000.00", now)
                .add(1, 1, "Salary from TNW Optimal Innovation LLC", TransactionType.DEPOSIT, "4200.00")
                .add(40, 10, "First installment to buy Facebook", TransactionType.PAYMENT, "1800.00")
                .add(28, 18, "Coffee with Jennifer Lawrence", TransactionType.PAYMENT, "142.35")
                .add(25, 8, "Electricity bill of White House", TransactionType.PAYMENT, "96.40")
                .add(21, 13, "Transfer from Elon", TransactionType.TRANSFER_IN, "350.00")
                .add(18, 9, "Salary from TNW Optimal Innovation LLC", TransactionType.DEPOSIT, "4200.00")
                .add(17, 10, "Apartment rent", TransactionType.PAYMENT, "1800.00")
                .add(12, 20, "Online order #48213", TransactionType.PAYMENT, "89.99")
                .add(9, 11, "Refund for order #48213", TransactionType.REFUND, "89.99")
                .add(6, 16, "ATM cash withdrawal", TransactionType.WITHDRAWAL, "200.00")
                .add(4, 12, "Transfer to savings", TransactionType.TRANSFER_OUT, "1250.00")
                .add(2, 7, "Corner Café", TransactionType.PAYMENT, "6.25")
                .add(1, 3, "Monthly account fee", TransactionType.FEE, "5.00");
        accountList.add(usd.toAccount("USD Account"));
        ledgers.put(USD_ACCOUNT_ID, usd.transactions());

        Ledger eur = new Ledger(EUR_ACCOUNT_ID, "EUR", "3500.00", now)
                .add(45, 10, "Freelance invoice INV-1042", TransactionType.DEPOSIT, "1200.00")
                .add(27, 15, "Rail tickets", TransactionType.PAYMENT, "84.50")
                .add(20, 19, "Hotel stay, Lisbon", TransactionType.PAYMENT, "312.00")
                .add(14, 9, "Freelance invoice INV-1057", TransactionType.DEPOSIT, "900.00")
                .add(10, 21, "Trattoria Roma", TransactionType.PAYMENT, "58.70")
                .add(5, 14, "Transfer to Maria Rossi", TransactionType.TRANSFER_OUT, "320.00")
                .add(3, 11, "Museum tickets", TransactionType.PAYMENT, "4.80");
        accountList.add(eur.toAccount("EUR Account"));
        ledgers.put(EUR_ACCOUNT_ID, eur.transactions());

        Ledger sgd = new Ledger(SGD_ACCOUNT_ID, "SGD", "7000.00", now)
                .add(33, 9, "Salary from Google", TransactionType.DEPOSIT, "3000.00")
                .add(29, 10, "House rent", TransactionType.PAYMENT, "450.00")
                .add(22, 13, "Cruise ticket", TransactionType.PAYMENT, "12.80")
                .add(15, 17, "Transfer to savings", TransactionType.TRANSFER_OUT, "1500.00")
                .add(11, 9, "Card cashback", TransactionType.DEPOSIT, "27.20")
                .add(7, 8, "Credit card fee", TransactionType.PAYMENT, "30.00")
                .add(3, 19, "Transfer from Tan Wei Ling", TransactionType.TRANSFER_IN, "215.60");
        accountList.add(sgd.toAccount("SGD Account"));
        ledgers.put(SGD_ACCOUNT_ID, sgd.transactions());

        Ledger npr = new Ledger(NPR_ACCOUNT_ID, "NPR", "0.00", now)
                .add(60, 10, "Bank Load", TransactionType.DEPOSIT, "80000.00")
                .add(55, 20, "Guesthouse stay, Pokhara", TransactionType.PAYMENT, "22000.00");
        accountList.add(npr.toAccount("NPR Travel Account"));
        ledgers.put(NPR_ACCOUNT_ID, npr.transactions());

        this.accounts = Collections.unmodifiableList(accountList);
        this.transactionsByAccount = Collections.unmodifiableMap(ledgers);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Transaction> getTransactions(String accountId) {
        List<Transaction> transactions = transactionsByAccount.get(accountId);
        return transactions == null ? Collections.emptyList() : transactions;
    }

    private static final class Ledger {

        private final String accountId;
        private final Currency currency;
        private final Instant now;
        private final List<Transaction> entries = new ArrayList<>();
        private BigDecimal balance;

        Ledger(String accountId, String currencyCode, String openingBalance, Instant now) {
            this.accountId = accountId;
            this.currency = Currency.getInstance(currencyCode);
            this.balance = new BigDecimal(openingBalance);
            this.now = now;
        }

        /** {@code unsignedAmount} is always written as a positive number; the type decides the sign. */
        Ledger add(int daysAgo, int hoursAgo, String description, TransactionType type, String unsignedAmount) {
            BigDecimal magnitude = new BigDecimal(unsignedAmount);
            BigDecimal amount = type.isCredit() ? magnitude : magnitude.negate();
            balance = balance.add(amount);
            String id = String.format(Locale.ROOT, "%s-txn-%03d", accountId, entries.size() + 1);
            Instant timestamp = now.minus(Duration.ofDays(daysAgo).plusHours(hoursAgo));
            entries.add(new Transaction(id, accountId, timestamp, description, type, amount, balance, currency));
            return this;
        }

        Account toAccount(String name) {
            return new Account(accountId, name, currency, balance);
        }

        List<Transaction> transactions() {
            return List.copyOf(entries);
        }
    }
}
