package com.example.demo.modules.equipment.repository;

import com.example.demo.modules.equipment.model.EquipmentAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentAlarmRepository extends JpaRepository<EquipmentAlarm, Long> {

    List<EquipmentAlarm> findByIsResolvedFalseOrderByCreatedAtDesc();
}