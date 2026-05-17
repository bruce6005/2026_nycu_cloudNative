package com.example.demo.modules.wip_builder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.wip_builder.model.TestRecords;
public interface TestRecordsRepository extends JpaRepository<TestRecords, Long> {
    List<TestRecords> findTop50ByOrderByStartTimeDesc();
}