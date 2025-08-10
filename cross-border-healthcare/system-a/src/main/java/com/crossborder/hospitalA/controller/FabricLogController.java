package com.crossborder.hospitalA.controller;

import com.crossborder.hospitalA.model.FabricLog;
import com.crossborder.hospitalA.repository.FabricLogRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.annotation.Cacheable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fabric-logs")
public class FabricLogController {

    @Autowired
    private FabricLogRepository fabricLogRepository;

    @Operation(summary = "Get paginated Fabric logs")
    @GetMapping
    public ResponseEntity<List<FabricLog>> getFabricLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                fabricLogRepository.findAllOrderByTimestampDesc(
                        PageRequest.of(page, size)
                )
        );
    }

    @Operation(summary = "Get Fabric logs analytics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics data retrieved"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/analytics")
    @Cacheable(value = "fabricAnalytics", key = "'daily'")
    public ResponseEntity<?> getFabricLogsAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();

            // Use LocalDateTime for last 7 days
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(7);

            analytics.put("timeSeries",
                    fabricLogRepository.getTimeSeriesData(startDate, endDate));
            analytics.put("statusDistribution",
                    fabricLogRepository.getStatusDistribution());
            analytics.put("doctorActivity",
                    fabricLogRepository.getDoctorActivity(startDate, endDate, PageRequest.of(0, 10)));

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
