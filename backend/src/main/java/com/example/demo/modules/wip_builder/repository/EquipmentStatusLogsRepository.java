package com.example.demo.modules.wip_builder.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.EquipmentStatusLogs;

public interface EquipmentStatusLogsRepository extends JpaRepository<EquipmentStatusLogs, Long> {
    Optional<EquipmentStatusLogs> findFirstByEquipmentIdOrderByStartTimeDesc(Long equipmentId);

    Optional<EquipmentStatusLogs> findFirstByEquipmentIdAndEndTimeIsNullOrderByStartTimeDesc(Long equipmentId);
}