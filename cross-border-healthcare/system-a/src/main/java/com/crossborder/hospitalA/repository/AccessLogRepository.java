package com.crossborder.hospitalA.repository;

import com.crossborder.hospitalA.model.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {}
