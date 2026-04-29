package com.example.demo.modules.dispatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.Sample;

public interface SampleRepository extends JpaRepository<Sample, Long> {
    List<Sample> findByBatchIsNull();
}
