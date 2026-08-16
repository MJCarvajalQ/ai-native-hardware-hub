package com.hardwarehub.seed;

import org.junit.jupiter.api.Test;

import com.hardwarehub.model.HardwareStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusNormalizerTest {

    private final StatusNormalizer normalizer = new StatusNormalizer();

    @Test
    void parsesTheThreeValidStatuses() {
        assertThat(normalizer.normalize("Available")).isEqualTo(HardwareStatus.AVAILABLE);
        assertThat(normalizer.normalize("In Use")).isEqualTo(HardwareStatus.IN_USE);
        assertThat(normalizer.normalize("Repair")).isEqualTo(HardwareStatus.REPAIR);
    }

    @Test
    void throwsOnUnknownStatus() {
        // seed record id 10 has status "Unknown" — must not be silently guessed
        assertThatThrownBy(() -> normalizer.normalize("Unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsOnNullStatus() {
        assertThatThrownBy(() -> normalizer.normalize(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
