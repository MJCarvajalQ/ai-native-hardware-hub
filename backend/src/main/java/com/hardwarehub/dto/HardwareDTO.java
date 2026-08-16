package com.hardwarehub.dto;

import com.hardwarehub.model.Hardware;
import com.hardwarehub.model.HardwareStatus;

import java.time.LocalDate;

/**
 * What the API actually returns for a piece of hardware. Kept separate from
 * the Hardware entity so internal fields (seedId) never get serialized to
 * clients, and so the entity can change shape without breaking the API.
 */
public record HardwareDTO(
        Long id,
        String name,
        String brand,
        LocalDate purchaseDate,
        HardwareStatus status,
        String notes,
        String history,
        String assignedTo
) {

    public static HardwareDTO from(Hardware hardware) {
        return new HardwareDTO(
                hardware.getId(),
                hardware.getName(),
                hardware.getBrand(),
                hardware.getPurchaseDate(),
                hardware.getStatus(),
                hardware.getNotes(),
                hardware.getHistory(),
                hardware.getAssignedTo()
        );
    }
}
