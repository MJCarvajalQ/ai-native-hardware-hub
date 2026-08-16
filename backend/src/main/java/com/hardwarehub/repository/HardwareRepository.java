package com.hardwarehub.repository;

import com.hardwarehub.model.Hardware;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HardwareRepository extends JpaRepository<Hardware, Long> {
}
