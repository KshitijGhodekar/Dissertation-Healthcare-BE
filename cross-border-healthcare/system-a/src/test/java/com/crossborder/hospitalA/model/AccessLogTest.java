package com.crossborder.hospitalA.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccessLogTest {

    @Test
    void testAccessLogFields() {
        AccessLog log = new AccessLog();
        log.setDoctorId("D001");
        log.setDoctorName("Dr. Smith");
        log.setPatientId("P001");
        log.setPurpose("treatment");
        log.setHospitalName("India Hospital");
        log.setTimestamp("2025-07-25T14:00:00Z");
        log.setAccessGranted(true);

        assertEquals("D001", log.getDoctorId());
        assertEquals("Dr. Smith", log.getDoctorName());
        assertEquals("P001", log.getPatientId());
        assertEquals("treatment", log.getPurpose());
        assertEquals("India Hospital", log.getHospitalName());
        assertEquals("2025-07-25T14:00:00Z", log.getTimestamp());
        assertTrue(log.isAccessGranted());
    }
}
