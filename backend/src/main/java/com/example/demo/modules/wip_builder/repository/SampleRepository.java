package com.example.demo.modules.wip_builder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.Sample;

public interface SampleRepository extends JpaRepository<Sample, Long> {
    List<Sample> findByBatchIsNull();
    List<Sample> findByBatch_Id(Long batchId);
    List<Sample> findByRequest_Id(Long requestId);
}
