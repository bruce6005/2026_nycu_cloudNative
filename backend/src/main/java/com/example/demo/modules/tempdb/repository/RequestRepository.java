package com.example.demo.modules.tempdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modules.tempdb.model.Request;

public interface RequestRepository extends JpaRepository<Request, Long> {

}
