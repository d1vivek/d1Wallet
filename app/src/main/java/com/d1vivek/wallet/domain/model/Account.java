package com.d1vivek.wallet.domain.model;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public final class Account {

    private final String id;
    private final String name;
    private final Currency currency;
    private final BigDecimal balance;

    public Account(String id, String name, Currency currency, BigDecimal balance) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.balance = Money.requireCurrencyScale(Objects.requireNonNull(balance, "balance"), currency);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Account that)) return false;
        return id.equals(that.id)
                && name.equals(that.name)
                && currency.equals(that.currency)
                && balance.equals(that.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, currency, balance);
    }
}
