package com.crossborder.hospitalA.model;

import org.junit.jupiter.api.Test;
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
        log.setTimestamp("2025-07-25T14:10:00Z");

        assertEquals(1L, log.getId());
        assertEquals("D002", log.getDoctorId());
        assertEquals("Dr. Jane", log.getDoctorName());
        assertEquals("P999", log.getPatientId());
        assertEquals("success", log.getStatus());
        assertEquals("2025-07-25T14:10:00Z", log.getTimestamp());
    }
}
