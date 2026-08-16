package com.hardwarehub.seed;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Reads seed-data.json as-is. Deliberately does no cleanup here — every
 * correction to the raw data happens later and is logged, per the task's
 * data audit requirement.
 */
public class SeedLoader {

    private static final String SEED_FILE = "/seed-data.json";

    public List<SeedRecord> loadRaw() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = SeedLoader.class.getResourceAsStream(SEED_FILE)) {
            if (in == null) {
                throw new IllegalStateException("seed-data.json not found on classpath");
            }
            return mapper.readValue(in, mapper.getTypeFactory()
                    .constructCollectionType(List.class, SeedRecord.class));
        } catch (IOException e) {
            throw new IllegalStateException("failed to parse seed-data.json", e);
        }
    }
}
