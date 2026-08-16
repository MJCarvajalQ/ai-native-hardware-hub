package com.hardwarehub.seed;

import com.hardwarehub.model.HardwareStatus;

/**
 * Strict parser for the status strings found in the seed data. Throws on
 * anything it doesn't recognize (including the seed's "Unknown" value)
 * rather than guessing — deciding what to do about an invalid status is a
 * recovery-policy decision, not a parsing decision, and belongs one layer
 * up, in whatever calls this (see DataSeeder). Mirrors DateNormalizer.
 */
public class StatusNormalizer {

    public HardwareStatus normalize(String rawStatus) {
        if (rawStatus == null) {
            throw new IllegalArgumentException("status is null");
        }
        switch (rawStatus) {
            case "Available":
                return HardwareStatus.AVAILABLE;
            case "In Use":
                return HardwareStatus.IN_USE;
            case "Repair":
                return HardwareStatus.REPAIR;
            default:
                throw new IllegalArgumentException("unrecognized status: " + rawStatus);
        }
    }
}
