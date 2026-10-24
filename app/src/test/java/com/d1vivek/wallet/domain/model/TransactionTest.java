package com.d1vivek.wallet.domain.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public class TransactionTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency NPR = Currency.getInstance("NPR");

    @Test
    public void creditTypesNeedPositiveAmounts() {
        assertThrows(IllegalArgumentException.class,
                () -> transaction(TransactionType.DEPOSIT, "-10.00", USD));
        assertEquals(new BigDecimal("10.00"), transaction(TransactionType.DEPOSIT, "10.00", USD).getAmount());
    }

    @Test
    public void debitTypesNeedNegativeAmounts() {
        assertThrows(IllegalArgumentException.class,
                () -> transaction(TransactionType.PAYMENT, "10.00", USD));
        assertEquals(new BigDecimal("-10.00"), transaction(TransactionType.PAYMENT, "-10.00", USD).getAmount());
    }

    @Test
    public void zeroAmountsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> transaction(TransactionType.FEE, "0.00", USD));
    }

    @Test
    public void amountsMustUseTheCurrencyScale() {
        assertThrows(IllegalArgumentException.class,
                () -> transaction(TransactionType.DEPOSIT, "10.5", USD));
        assertThrows(IllegalArgumentException.class,
                () -> transaction(TransactionType.DEPOSIT, "500", NPR));
        assertEquals(new BigDecimal("500.00"), transaction(TransactionType.DEPOSIT, "500.00", NPR).getAmount());
    }

    private static Transaction transaction(TransactionType type, String amount, Currency currency) {
        BigDecimal value = new BigDecimal(amount);
        return new Transaction("t", "acc", Instant.EPOCH, "Test", type, value, value.abs(), currency);
    }
}
