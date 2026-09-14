package com.logistics.repository;

import com.logistics.entity.Load;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadRepository extends JpaRepository<Load, Long> {
    long countByStatus(String status);
}
