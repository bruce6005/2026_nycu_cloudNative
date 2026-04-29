package com.example.demo.modules.dispatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.WIPbatch;

public interface WIPbatchRepository extends JpaRepository<WIPbatch, Long> {
}
