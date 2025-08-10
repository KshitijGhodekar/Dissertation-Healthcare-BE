package com.crossborder.hospitalA.repository;

import com.crossborder.hospitalA.model.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // For table (latest logs) with pagination and total count
    @Query(
            value = "SELECT al FROM AccessLog al ORDER BY al.timestamp DESC",
            countQuery = "SELECT COUNT(al) FROM AccessLog al"
    )
    Page<AccessLog> findAllOrderByTimestampDesc(Pageable pageable);

    // Time series: granted vs denied access
    @Query("SELECT new map(" +
            "FUNCTION('DATE', al.timestamp) as date, " +
            "SUM(CASE WHEN al.accessGranted = true THEN 1 ELSE 0 END) as granted, " +
            "SUM(CASE WHEN al.accessGranted = false THEN 1 ELSE 0 END) as denied, " +
            "COUNT(al) as total) " +
            "FROM AccessLog al " +
            "WHERE al.timestamp BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', al.timestamp) " +
            "ORDER BY date")
    List<Map<String, Object>> getTimeSeriesData(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Access distribution (Granted vs Denied)
    @Query("SELECT new map(" +
            "CASE WHEN al.accessGranted = true THEN 'granted' ELSE 'denied' END as name, " +
            "COUNT(al) as value) " +
            "FROM AccessLog al " +
            "GROUP BY al.accessGranted")
    List<Map<String, Object>> getAccessDistribution();

    // Top doctors by number of access attempts
    @Query("SELECT new map(" +
            "al.doctorName as name, " +
            "al.doctorId as doctorId, " +
            "COUNT(al) as attempts, " +
            "SUM(CASE WHEN al.accessGranted = true THEN 1 ELSE 0 END) as granted, " +
            "SUM(CASE WHEN al.accessGranted = false THEN 1 ELSE 0 END) as denied) " +
            "FROM AccessLog al " +
            "WHERE al.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY al.doctorId, al.doctorName " +
            "ORDER BY attempts DESC")
    List<Map<String, Object>> getDoctorActivity(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
