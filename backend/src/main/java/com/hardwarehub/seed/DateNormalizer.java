package com.hardwarehub.seed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Normalizes the purchaseDate strings found in the seed data. The seed
 * deliberately mixes ISO dates with at least one DD-MM-YYYY date and one
 * null, so this class tries each known format before giving up.
 */
public class DateNormalizer {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Returns the parsed date, or null if the input was null/blank.
     * Throws IllegalArgumentException if the input is non-null but matches
     * neither known format, so an unexpected new format is never silently
     * misread as a different date.
     */
    public LocalDate normalize(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate, ISO);
        } catch (Exception isoFailed) {
            try {
                return LocalDate.parse(rawDate, DAY_MONTH_YEAR);
            } catch (Exception dayMonthYearFailed) {
                throw new IllegalArgumentException("unrecognized date format: " + rawDate);
            }
        }
    }
}
