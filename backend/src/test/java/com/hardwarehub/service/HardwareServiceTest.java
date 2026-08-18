package com.hardwarehub.service;

import com.hardwarehub.exception.IllegalHardwareStateException;
import com.hardwarehub.model.Hardware;
import com.hardwarehub.model.HardwareStatus;
import com.hardwarehub.repository.HardwareRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The 3 critical tests the task requires, generated/guided by AI — see
 * PROMPTS.md for the prompt that produced them.
 */
@ExtendWith(MockitoExtension.class)
class HardwareServiceTest {

    @Mock
    private HardwareRepository hardwareRepository;

    private HardwareService hardwareService;

    private Hardware hardwareWithStatus(HardwareStatus status) {
        Hardware hardware = new Hardware();
        hardware.setId(1L);
        hardware.setName("Test Device");
        hardware.setBrand("TestBrand");
        hardware.setStatus(status);
        return hardware;
    }

    @Test
    void cannotRentHardwareUnderRepair() {
        hardwareService = new HardwareService(hardwareRepository);
        Hardware hardware = hardwareWithStatus(HardwareStatus.REPAIR);
        when(hardwareRepository.findById(1L)).thenReturn(Optional.of(hardware));

        assertThatThrownBy(() -> hardwareService.rent(1L, "user@example.com"))
                .isInstanceOf(IllegalHardwareStateException.class);
    }

    @Test
    void cannotRentHardwareAlreadyInUse() {
        hardwareService = new HardwareService(hardwareRepository);
        Hardware hardware = hardwareWithStatus(HardwareStatus.IN_USE);
        when(hardwareRepository.findById(1L)).thenReturn(Optional.of(hardware));

        assertThatThrownBy(() -> hardwareService.rent(1L, "user@example.com"))
                .isInstanceOf(IllegalHardwareStateException.class);
    }

    @Test
    void cannotDeleteHardwareCurrentlyInUse() {
        hardwareService = new HardwareService(hardwareRepository);
        Hardware hardware = hardwareWithStatus(HardwareStatus.IN_USE);
        when(hardwareRepository.findById(1L)).thenReturn(Optional.of(hardware));

        assertThatThrownBy(() -> hardwareService.delete(1L))
                .isInstanceOf(IllegalHardwareStateException.class);
    }

    @Test
    void rentThenReturnRestoresAvailableAndClearsAssignedTo() {
        hardwareService = new HardwareService(hardwareRepository);
        Hardware hardware = hardwareWithStatus(HardwareStatus.AVAILABLE);
        when(hardwareRepository.findById(1L)).thenReturn(Optional.of(hardware));
        when(hardwareRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Hardware rented = hardwareService.rent(1L, "user@example.com");
        assertThat(rented.getStatus()).isEqualTo(HardwareStatus.IN_USE);
        assertThat(rented.getAssignedTo()).isEqualTo("user@example.com");

        Hardware returned = hardwareService.returnItem(1L, "user@example.com");
        assertThat(returned.getStatus()).isEqualTo(HardwareStatus.AVAILABLE);
        assertThat(returned.getAssignedTo()).isNull();
    }
}
