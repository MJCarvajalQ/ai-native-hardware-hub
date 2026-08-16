package com.hardwarehub.controller;

import com.hardwarehub.dto.CreateHardwareRequest;
import com.hardwarehub.dto.HardwareDTO;
import com.hardwarehub.model.HardwareStatus;
import com.hardwarehub.model.User;
import com.hardwarehub.service.HardwareService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hardware")
public class HardwareController {

    private final HardwareService hardwareService;

    public HardwareController(HardwareService hardwareService) {
        this.hardwareService = hardwareService;
    }

    @GetMapping
    public List<HardwareDTO> list(
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) HardwareStatus status,
            @RequestParam(required = false) String brand
    ) {
        return hardwareService.list(sortBy, direction, status, brand)
                .stream()
                .map(HardwareDTO::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HardwareDTO create(@Valid @RequestBody CreateHardwareRequest request) {
        return HardwareDTO.from(hardwareService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hardwareService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rent")
    public HardwareDTO rent(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return HardwareDTO.from(hardwareService.rent(id, currentUser.getEmail()));
    }

    @PostMapping("/{id}/return")
    public HardwareDTO returnItem(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return HardwareDTO.from(hardwareService.returnItem(id, currentUser.getEmail()));
    }

    @PatchMapping("/{id}/repair")
    public HardwareDTO toggleRepair(@PathVariable Long id) {
        return HardwareDTO.from(hardwareService.toggleRepair(id));
    }
}
