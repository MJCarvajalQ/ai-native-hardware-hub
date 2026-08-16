package com.hardwarehub.service;

import com.hardwarehub.dto.CreateHardwareRequest;
import com.hardwarehub.exception.HardwareNotFoundException;
import com.hardwarehub.exception.IllegalHardwareStateException;
import com.hardwarehub.model.Hardware;
import com.hardwarehub.model.HardwareStatus;
import com.hardwarehub.repository.HardwareRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

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

    /**
     * Fetches everything and filters/sorts in memory. With 11 seed rows and
     * no realistic growth expected for an internal tool this size, that's
     * simpler and just as fast as building a JPA Specification would be —
     * not a shortcut, a proportionate choice for the data volume.
     */
    public List<Hardware> list(String sortBy, String direction, HardwareStatus statusFilter, String brandFilter) {
        List<Hardware> hardware = hardwareRepository.findAll();

        if (statusFilter != null) {
            hardware = hardware.stream().filter(h -> h.getStatus() == statusFilter).toList();
        }
        if (brandFilter != null && !brandFilter.isBlank()) {
            hardware = hardware.stream()
                    .filter(h -> brandFilter.equalsIgnoreCase(h.getBrand()))
                    .toList();
        }

        Comparator<Hardware> comparator = comparatorFor(sortBy);
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        return hardware.stream().sorted(comparator).toList();
    }

    // Falls back to sorting by name for an unrecognized sortBy value rather
    // than rejecting the request — this is a permissive API parameter, not
    // seed data whose correctness matters, so a sensible default is fine
    // here in a way it wasn't in DataSeeder.
    private Comparator<Hardware> comparatorFor(String sortBy) {
        return switch (sortBy) {
            case "brand" -> Comparator.comparing(Hardware::getBrand, String.CASE_INSENSITIVE_ORDER);
            case "purchaseDate" -> Comparator.comparing(
                    Hardware::getPurchaseDate, Comparator.nullsFirst(Comparator.naturalOrder()));
            case "status" -> Comparator.comparing(h -> h.getStatus().name());
            default -> Comparator.comparing(Hardware::getName, String.CASE_INSENSITIVE_ORDER);
        };
    }

    public Hardware create(CreateHardwareRequest request) {
        Hardware hardware = new Hardware();
        hardware.setName(request.name());
        hardware.setBrand(request.brand());
        hardware.setPurchaseDate(request.purchaseDate());
        hardware.setNotes(request.notes());
        hardware.setStatus(HardwareStatus.AVAILABLE);
        return hardwareRepository.save(hardware);
    }

    public void delete(Long hardwareId) {
        getOrThrow(hardwareId);
        hardwareRepository.deleteById(hardwareId);
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
