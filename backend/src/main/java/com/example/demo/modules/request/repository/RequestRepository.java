package com.example.demo.modules.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.example.demo.modules.request.model.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByApprover_IdAndStatus(Long approverId, String status);
}
