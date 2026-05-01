package com.example.demo.modules.requests.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.requests.model.Requests;
import com.example.demo.modules.requests.model.RequestsStatus;

public interface RequestsRepository extends JpaRepository<Requests, Long> {
    List<Requests> findByStatus(RequestsStatus status);
    List<Requests> findByApproverIdAndStatus(Long approverId, RequestsStatus status);
}