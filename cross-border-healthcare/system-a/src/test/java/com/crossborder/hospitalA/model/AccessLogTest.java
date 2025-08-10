package com.crossborder.hospitalA.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class AccessLogTest {

    @Test
    void testAccessLogFields() {
        AccessLog log = new AccessLog();
        log.setDoctorId("D001");
        log.setDoctorName("Dr. Smith");
        log.setPatientId("P001");
        log.setPurpose("treatment");
        log.setHospitalName("India Hospital");
        LocalDateTime timestamp = LocalDateTime.parse("2025-07-25T14:10:00");
        log.setTimestamp(timestamp);
        log.setAccessGranted(true);

        assertEquals("D001", log.getDoctorId());
        assertEquals("Dr. Smith", log.getDoctorName());
        assertEquals("P001", log.getPatientId());
        assertEquals("treatment", log.getPurpose());
        assertEquals("India Hospital", log.getHospitalName());
        String formatted = log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assertEquals("2025-07-25T14:10:00", formatted);
        assertTrue(log.isAccessGranted());
    }
}
