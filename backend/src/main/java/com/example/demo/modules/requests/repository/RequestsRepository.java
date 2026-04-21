package com.example.demo.modules.requests.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.requests.model.Requests;

public interface RequestsRepository extends JpaRepository<Requests, Long> {

    List<Requests> findByApproverIdAndStatus(Long approverId, String status);
}