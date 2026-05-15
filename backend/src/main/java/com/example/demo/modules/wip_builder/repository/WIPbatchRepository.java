package com.example.demo.modules.wip_builder.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.wip_builder.model.WIPbatch;
import java.util.List;

public interface WIPbatchRepository extends JpaRepository<WIPbatch, Long> {

    List<WIPbatch> findByStatus(String status);
    List<WIPbatch> findByStatusIn(List<String> statuses);

}
