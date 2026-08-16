package com.hardwarehub.seed;

import com.hardwarehub.model.Hardware;
import com.hardwarehub.model.HardwareStatus;
import com.hardwarehub.repository.HardwareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads seed-data.json on startup. The file is known to contain a duplicate
 * id, a malformed date, a null date, an invalid status, an empty brand, and
 * a brand typo — this class is where every one of those gets handled, and
 * every finding is logged so the log can be pasted into the README's Data
 * Strategy section.
 *
 * Only structural problems (unparseable dates, invalid statuses) get a
 * fallback value here. Everything else (blank brand, future purchase date,
 * missing purchase date) is loaded as-is and just logged, since there's
 * nothing unsafe about leaving it for a human to review.
 *
 * Every finding is logged and counted right where it's detected — nothing
 * is inferred afterward by comparing raw vs. normalized values, since that
 * approach silently misses no-op findings (e.g. a blank brand stays blank,
 * so a before/after comparison wouldn't see it as a "change").
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final HardwareRepository hardwareRepository;
    private final SeedLoader seedLoader;
    private final DateNormalizer dateNormalizer;
    private final StatusNormalizer statusNormalizer;

    public DataSeeder(HardwareRepository hardwareRepository) {
        this.hardwareRepository = hardwareRepository;
        this.seedLoader = new SeedLoader();
        this.dateNormalizer = new DateNormalizer();
        this.statusNormalizer = new StatusNormalizer();
    }

    @Override
    public void run(String... args) {
        if (hardwareRepository.count() > 0) {
            log.info("hardware table already has data, skipping seed");
            return;
        }

        log.info("seeding hardware from seed-data.json — data audit findings below");

        List<SeedRecord> rawRecords = seedLoader.loadRaw();
        Set<Long> seenSeedIds = new HashSet<>();
        int findings = 0;

        for (SeedRecord raw : rawRecords) {
            Long seedId = raw.getId();

            if (!seenSeedIds.add(seedId)) {
                log.warn("seed id {} appears more than once in seed-data.json ('{}'); "
                        + "loading it anyway as its own record, since this app assigns its own "
                        + "database ids and never trusts seed-provided ids as primary keys",
                        seedId, raw.getName());
                findings++;
            }

            Hardware hardware = new Hardware();
            hardware.setSeedId(seedId);
            hardware.setName(raw.getName());
            hardware.setNotes(raw.getNotes());
            hardware.setHistory(raw.getHistory());
            hardware.setAssignedTo(raw.getAssignedTo());

            // --- brand ---
            String rawBrand = raw.getBrand();
            if (rawBrand == null || rawBrand.isBlank()) {
                log.warn("seed id {}: brand is blank; leaving it blank", seedId);
                findings++;
                hardware.setBrand(rawBrand);
            } else if ("Appel".equals(rawBrand)) {
                log.warn("seed id {}: brand 'Appel' looks like a typo for 'Apple'; correcting it",
                        seedId);
                findings++;
                hardware.setBrand("Apple");
            } else {
                hardware.setBrand(rawBrand);
            }

            // --- purchase date ---
            String rawDate = raw.getPurchaseDate();
            if (rawDate == null) {
                log.info("seed id {}: purchaseDate is null; leaving it null", seedId);
                findings++;
                hardware.setPurchaseDate(null);
            } else {
                try {
                    LocalDate parsed = dateNormalizer.normalize(rawDate);
                    if (!parsed.toString().equals(rawDate)) {
                        log.warn("seed id {}: purchaseDate '{}' was not in ISO format; normalized to {}",
                                seedId, rawDate, parsed);
                        findings++;
                    }
                    if (parsed.isAfter(LocalDate.now())) {
                        log.warn("seed id {}: purchaseDate {} is in the future; loading it as-is",
                                seedId, parsed);
                        findings++;
                    }
                    hardware.setPurchaseDate(parsed);
                } catch (IllegalArgumentException e) {
                    log.warn("seed id {}: {}; storing purchaseDate as null", seedId, e.getMessage());
                    findings++;
                    hardware.setPurchaseDate(null);
                }
            }

            // --- status ---
            try {
                hardware.setStatus(statusNormalizer.normalize(raw.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("seed id {}: {}; defaulting status to REPAIR pending manual review",
                        seedId, e.getMessage());
                findings++;
                hardware.setStatus(HardwareStatus.REPAIR);
            }

            hardwareRepository.save(hardware);
        }

        log.info("seed complete: {} records loaded, {} data-quality findings", rawRecords.size(), findings);
    }
}
