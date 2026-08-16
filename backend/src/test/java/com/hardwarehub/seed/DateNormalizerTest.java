package com.hardwarehub.seed;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DateNormalizerTest {

    private final DateNormalizer normalizer = new DateNormalizer();

    @Test
    void parsesIsoDates() {
        assertThat(normalizer.normalize("2021-11-23")).isEqualTo(LocalDate.of(2021, 11, 23));
    }

    @Test
    void parsesDayMonthYearDates() {
        // seed record id 9: "22-05-2023" means 22 May 2023, not 5 Feb 1922
        assertThat(normalizer.normalize("22-05-2023")).isEqualTo(LocalDate.of(2023, 5, 22));
    }

    @Test
    void returnsNullForNull() {
        assertThat(normalizer.normalize(null)).isNull();
    }
}
