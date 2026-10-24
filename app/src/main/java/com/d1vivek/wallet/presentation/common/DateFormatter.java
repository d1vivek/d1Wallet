package com.d1vivek.wallet.presentation.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class DateFormatter {

    private final DateTimeFormatter dateTime;
    private final DateTimeFormatter date;

    public DateFormatter(Locale locale, ZoneId zone) {
        dateTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(zone);
        date = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .withZone(zone);
    }

    public String formatDateTime(Instant instant) {
        return dateTime.format(instant);
    }

    public String formatDate(Instant instant) {
        return date.format(instant);
    }
}
