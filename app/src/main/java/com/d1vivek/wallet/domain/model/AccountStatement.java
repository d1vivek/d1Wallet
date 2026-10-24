package com.d1vivek.wallet.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record AccountStatement(Account account, Instant from, Instant to,
                               List<Transaction> transactions) {

    public AccountStatement(Account account, Instant from, Instant to, List<Transaction> transactions) {
        this.account = Objects.requireNonNull(account, "account");
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.transactions = Collections.unmodifiableList(transactions);
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }
}
