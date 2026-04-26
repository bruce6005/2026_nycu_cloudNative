package com.example.demo.modules.requests.repository;

import com.example.demo.modules.requests.model.Requests;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestsRepository extends JpaRepository<Requests, Long> {
}