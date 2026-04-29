package com.example.demo.modules.dispatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
}
