package com.rtb.manageyourmoneybackend.expense.filter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * Predefined "created" date ranges a search request can pick instead of
 * supplying an explicit {@code createdFrom}/{@code createdTo} custom range.
 * <p>
 * All ranges are resolved against the server's default time zone at the
 * moment of the request. {@link #ALL_TIME} yields an open range (no filter).
 */
public enum DateRangePreset {

    ALL_TIME,
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    PREVIOUS_WEEK,
    PREVIOUS_MONTH,
    LAST_7_DAYS,
    LAST_30_DAYS,
    LAST_365_DAYS;

    /**
     * @return a two-element array {@code [from, to]}; either element may be
     * {@code null} to mean "unbounded" (only {@link #ALL_TIME} yields both null).
     */
    public Instant[] resolve() {
        ZoneId zone = ZoneId.systemDefault();
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(zone);

        return switch (this) {
            case ALL_TIME -> new Instant[]{null, null};

            case TODAY -> new Instant[]{
                    today.atStartOfDay(zone).toInstant(),
                    now
            };

            case THIS_WEEK -> new Instant[]{
                    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(zone).toInstant(),
                    now
            };

            case THIS_MONTH -> new Instant[]{
                    today.withDayOfMonth(1).atStartOfDay(zone).toInstant(),
                    now
            };

            case THIS_YEAR -> new Instant[]{
                    today.withDayOfYear(1).atStartOfDay(zone).toInstant(),
                    now
            };

            case PREVIOUS_WEEK -> {
                LocalDate previousMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
                LocalDate previousSunday = previousMonday.plusDays(6);
                yield new Instant[]{
                        previousMonday.atStartOfDay(zone).toInstant(),
                        previousSunday.plusDays(1).atStartOfDay(zone).toInstant()
                };
            }

            case PREVIOUS_MONTH -> {
                LocalDate firstOfThisMonth = today.withDayOfMonth(1);
                LocalDate firstOfPreviousMonth = firstOfThisMonth.minusMonths(1);
                yield new Instant[]{
                        firstOfPreviousMonth.atStartOfDay(zone).toInstant(),
                        firstOfThisMonth.atStartOfDay(zone).toInstant()
                };
            }

            case LAST_7_DAYS -> new Instant[]{now.minus(java.time.Duration.ofDays(7)), now};
            case LAST_30_DAYS -> new Instant[]{now.minus(java.time.Duration.ofDays(30)), now};
            case LAST_365_DAYS -> new Instant[]{now.minus(java.time.Duration.ofDays(365)), now};
        };
    }
}