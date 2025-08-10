package com.crossborder.hospitalA.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class FabricLogTest {

    @Test
    void testFabricLogFields() {
        FabricLog log = new FabricLog();
        log.setId(1L);
        log.setDoctorId("D002");
        log.setDoctorName("Dr. Jane");
        log.setPatientId("P999");
        log.setStatus("success");

        // Convert ISO string to LocalDateTime
        LocalDateTime timestamp = LocalDateTime.parse("2025-07-25T14:10:00");
        log.setTimestamp(timestamp);

        assertEquals(1L, log.getId());
        assertEquals("D002", log.getDoctorId());
        assertEquals("Dr. Jane", log.getDoctorName());
        assertEquals("P999", log.getPatientId());
        assertEquals("success", log.getStatus());
        assertEquals(timestamp, log.getTimestamp());

        String formatted = log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertEquals("2025-07-25T14:10:00", formatted);
    }
}
