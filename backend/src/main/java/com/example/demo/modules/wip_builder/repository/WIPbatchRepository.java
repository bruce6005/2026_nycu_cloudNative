package com.example.demo.modules.wip_builder.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.wip_builder.model.WIPbatch;
public interface WIPbatchRepository extends JpaRepository<WIPbatch, Long> {
    List<WIPbatch> findByStatus(String status);
    List<WIPbatch> findByStatusIn(List<String> statuses);
    List<WIPbatch> findByEquipment_Id(Long equipmentId);
    Optional<WIPbatch> findFirstByEquipment_IdAndStatusOrderByStartTimeDesc(Long equipmentId, String status);
}
