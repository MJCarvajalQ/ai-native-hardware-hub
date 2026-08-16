package com.hardwarehub.seed;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeedLoaderTest {

    @Test
    void loadsAllElevenRecordsWithoutThrowing() {
        List<SeedRecord> records = new SeedLoader().loadRaw();

        assertThat(records).hasSize(11);
    }
}
