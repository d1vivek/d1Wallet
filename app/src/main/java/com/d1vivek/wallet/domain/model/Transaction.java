package com.d1vivek.wallet.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;

public final class Transaction {

    private final String id;
    private final String accountId;
    private final Instant timestamp;
    private final String description;
    private final TransactionType type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final Currency currency;

    public Transaction(
            String id,
            String accountId,
            Instant timestamp,
            String description,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            Currency currency) {
        this.id = Objects.requireNonNull(id, "id");
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.description = Objects.requireNonNull(description, "description");
        this.type = Objects.requireNonNull(type, "type");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.amount = Money.requireCurrencyScale(Objects.requireNonNull(amount, "amount"), currency);
        this.balanceAfter = Money.requireCurrencyScale(
                Objects.requireNonNull(balanceAfter, "balanceAfter"), currency);

        int sign = amount.signum();
        if (sign == 0) {
            throw new IllegalArgumentException("Transaction " + id + " has a zero amount");
        }
        if (type.isCredit() != (sign > 0)) {
            throw new IllegalArgumentException(
                    "Transaction " + id + " is a " + type + " but its amount has the wrong sign");
        }
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Transaction that)) return false;
        return id.equals(that.id)
                && accountId.equals(that.accountId)
                && timestamp.equals(that.timestamp)
                && description.equals(that.description)
                && type == that.type
                && amount.equals(that.amount)
                && balanceAfter.equals(that.balanceAfter)
                && currency.equals(that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, timestamp, description, type, amount, balanceAfter, currency);
    }
}
