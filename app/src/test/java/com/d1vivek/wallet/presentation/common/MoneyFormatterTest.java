package com.d1vivek.wallet.presentation.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

public class MoneyFormatterTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency NPR = Currency.getInstance("NPR");

    private final MoneyFormatter formatter = new MoneyFormatter(Locale.US);

    @Test
    public void formatsWithGroupingAndTwoDecimals() {
        assertEquals("$12,450.00", formatter.format(new BigDecimal("12450.00"), USD));
    }

    @Test
    public void formatsNepaleseRupeesWithTwoDecimals() {
        assertEquals("NPR58,000.00", formatter.format(new BigDecimal("58000.00"), NPR));
    }

    @Test
    public void signedFormatShowsPlusForCreditsAndMinusForDebits() {
        assertEquals("+$350.00", formatter.formatSigned(new BigDecimal("350.00"), USD));
        assertEquals("−$6.25", formatter.formatSigned(new BigDecimal("-6.25"), USD));
    }

    @Test
    public void keepsExactCentsForLargeAmounts() {
        assertEquals("$9,999,999,999,999.99",
                formatter.format(new BigDecimal("9999999999999.99"), USD));
    }
}
