package com.d1vivek.wallet.domain.model;

import java.math.BigDecimal;
import java.util.Currency;

public final class Money {

    private Money() {
    }

    public static BigDecimal requireCurrencyScale(BigDecimal amount, Currency currency) {
        int expectedScale = currency.getDefaultFractionDigits();
        if (expectedScale >= 0 && amount.scale() != expectedScale) {
            throw new IllegalArgumentException(
                    "Expected scale " + expectedScale + " for " + currency.getCurrencyCode()
                            + " but got " + amount.scale());
        }
        return amount;
    }
}
