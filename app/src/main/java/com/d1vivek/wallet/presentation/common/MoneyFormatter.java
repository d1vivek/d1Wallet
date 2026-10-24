package com.d1vivek.wallet.presentation.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class MoneyFormatter {

    private static final String PLUS = "+";
    private static final String MINUS = "−";

    private final Locale locale;

    public MoneyFormatter(Locale locale) {
        this.locale = locale;
    }

    public String format(BigDecimal amount, Currency currency) {
        return currencyFormat(currency).format(amount);
    }

    /** Always shows the sign, e.g. "+$350.00" or "−$6.25". */
    public String formatSigned(BigDecimal amount, Currency currency) {
        String magnitude = format(amount.abs(), currency);
        return switch (amount.signum()) {
            case 1 -> PLUS + magnitude;
            case -1 -> MINUS + magnitude;
            default -> magnitude;
        };
    }

    private NumberFormat currencyFormat(Currency currency) {
        // NumberFormat isn't thread-safe, so make a fresh one rather than caching it.
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setCurrency(currency);
        int fractionDigits = currency.getDefaultFractionDigits();
        if (fractionDigits >= 0) {
            format.setMinimumFractionDigits(fractionDigits);
            format.setMaximumFractionDigits(fractionDigits);
        }
        return format;
    }
}
