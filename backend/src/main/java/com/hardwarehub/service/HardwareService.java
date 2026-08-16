package com.hardwarehub.service;

import com.hardwarehub.exception.HardwareNotFoundException;
import com.hardwarehub.exception.IllegalHardwareStateException;
import com.hardwarehub.model.Hardware;
import com.hardwarehub.model.HardwareStatus;
import com.hardwarehub.repository.HardwareRepository;
import org.springframework.stereotype.Service;

/**
 * Owns every rental state transition. This is the one place that enforces
 * the rule the task asks for: renting/returning/repairing must never put a
 * piece of hardware into an impossible state. The controller (Block F) is
 * not trusted to have already checked this — every rule is re-checked here.
 */
@Service
public class HardwareService {

    private final HardwareRepository hardwareRepository;

    public HardwareService(HardwareRepository hardwareRepository) {
        this.hardwareRepository = hardwareRepository;
    }

    public Hardware rent(Long hardwareId, String userEmail) {
        Hardware hardware = getOrThrow(hardwareId);
        if (hardware.getStatus() != HardwareStatus.AVAILABLE) {
            throw new IllegalHardwareStateException(
                    "cannot rent hardware " + hardwareId + ": status is " + hardware.getStatus());
        }
        hardware.setStatus(HardwareStatus.IN_USE);
        hardware.setAssignedTo(userEmail);
        return hardwareRepository.save(hardware);
    }

    public Hardware returnItem(Long hardwareId, String userEmail) {
        Hardware hardware = getOrThrow(hardwareId);
        if (hardware.getStatus() != HardwareStatus.IN_USE) {
            throw new IllegalHardwareStateException(
                    "cannot return hardware " + hardwareId + ": status is " + hardware.getStatus());
        }
        if (!userEmail.equals(hardware.getAssignedTo())) {
            throw new IllegalHardwareStateException(
                    "cannot return hardware " + hardwareId + ": not assigned to " + userEmail);
        }
        hardware.setStatus(HardwareStatus.AVAILABLE);
        hardware.setAssignedTo(null);
        return hardwareRepository.save(hardware);
    }

    public Hardware toggleRepair(Long hardwareId) {
        Hardware hardware = getOrThrow(hardwareId);
        HardwareStatus currentStatus = hardware.getStatus();

        if (currentStatus == HardwareStatus.IN_USE) {
            throw new IllegalHardwareStateException(
                    "cannot change repair status for hardware " + hardwareId
                            + ": it is currently in use, return it first");
        } else if (currentStatus == HardwareStatus.AVAILABLE) {
            hardware.setStatus(HardwareStatus.REPAIR);
        } else if (currentStatus == HardwareStatus.REPAIR) {
            hardware.setStatus(HardwareStatus.AVAILABLE);
        } else {
            throw new IllegalStateException("unexpected hardware status: " + currentStatus);
        }

        return hardwareRepository.save(hardware);
    }

    private Hardware getOrThrow(Long hardwareId) {
        return hardwareRepository.findById(hardwareId)
                .orElseThrow(() -> new HardwareNotFoundException(hardwareId));
    }
}
