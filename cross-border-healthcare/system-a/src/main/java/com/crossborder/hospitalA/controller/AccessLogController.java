package com.crossborder.hospitalA.controller;

import com.crossborder.hospitalA.model.AccessLog;
import com.crossborder.hospitalA.repository.AccessLogRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/access-logs")
public class AccessLogController {

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Operation(summary = "Get paginated Access logs with total count")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAccessLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Fetch paginated logs
        Page<AccessLog> logsPage = accessLogRepository.findAllOrderByTimestampDesc(
                PageRequest.of(page, size)
        );

        // Prepare response with logs and total count
        Map<String, Object> response = new HashMap<>();
        response.put("logs", logsPage.getContent());
        response.put("totalLogs", logsPage.getTotalElements());
        response.put("currentPage", logsPage.getNumber());
        response.put("totalPages", logsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Access logs analytics")
    @GetMapping("/analytics")
    public ResponseEntity<?> getAccessLogsAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(7);

            analytics.put("timeSeries",
                    accessLogRepository.getTimeSeriesData(startDate, endDate));
            analytics.put("accessDistribution",
                    accessLogRepository.getAccessDistribution());
            analytics.put("doctorActivity",
                    accessLogRepository.getDoctorActivity(startDate, endDate, PageRequest.of(0, 10)));

            // Also include total logs in analytics
            analytics.put("totalLogs", accessLogRepository.count());

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Analytics generation failed",
                            "details", e.getMessage()
                    ));
        }
    }
}
