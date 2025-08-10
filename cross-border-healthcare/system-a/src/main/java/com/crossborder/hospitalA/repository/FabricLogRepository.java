package com.crossborder.hospitalA.repository;

import com.crossborder.hospitalA.model.FabricLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface FabricLogRepository extends JpaRepository<FabricLog, Long> {

    // For main logs listing
    @Query("SELECT fl FROM FabricLog fl ORDER BY fl.timestamp DESC")
    List<FabricLog> findAllOrderByTimestampDesc(Pageable pageable);

    // Time series with totals
    @Query("SELECT new map(" +
            "FUNCTION('DATE', fl.timestamp) as date, " +
            "SUM(CASE WHEN fl.status = 'granted' THEN 1 ELSE 0 END) as granted, " +
            "SUM(CASE WHEN fl.status = 'denied' THEN 1 ELSE 0 END) as denied, " +
            "COUNT(fl) as total) " +
            "FROM FabricLog fl " +
            "WHERE fl.timestamp BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', fl.timestamp) " +
            "ORDER BY date")
    List<Map<String, Object>> getTimeSeriesData(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Status distribution with null handling
    @Query("SELECT new map(" +
            "COALESCE(fl.status, 'UNKNOWN') as name, " +
            "COUNT(fl) as value) " +
            "FROM FabricLog fl " +
            "GROUP BY COALESCE(fl.status, 'UNKNOWN')")
    List<Map<String, Object>> getStatusDistribution();

    // Doctor activity with date filtering
    @Query("SELECT new map(" +
            "fl.doctorName as name, " +
            "fl.doctorId as doctorId, " +
            "COUNT(fl) as transactions, " +
            "SUM(CASE WHEN fl.status = 'granted' THEN 1 ELSE 0 END) as granted, " +
            "SUM(CASE WHEN fl.status = 'denied' THEN 1 ELSE 0 END) as denied) " +
            "FROM FabricLog fl " +
            "WHERE fl.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY fl.doctorId, fl.doctorName " +
            "ORDER BY transactions DESC")
    List<Map<String, Object>> getDoctorActivity(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}