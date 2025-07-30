package com.crossborder.hospitalA.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class PatientDataRequestTest {

    @Test
    void testConstructorAndFields() {
        PatientDataRequest request = new PatientDataRequest(
                "req123", "D123", "Dr. Adams", "P123",
                "2025-07-25T14:30:00Z", "diagnosis", "India Hospital"
        );

        request.setMobileNumber("9876543210");
        request.setPatientIds(Arrays.asList("P123", "P124"));

        assertEquals("req123", request.getRequestId());
        assertEquals("D123", request.getDoctorId());
        assertEquals("Dr. Adams", request.getDoctorName());
        assertEquals("P123", request.getPatientId());
        assertEquals("diagnosis", request.getPurpose());
        assertEquals("India Hospital", request.getHospitalName());
        assertEquals("9876543210", request.getMobileNumber());
        assertEquals(2, request.getPatientIds().size());
        assertEquals("2025-07-25T14:30:00Z", request.getTimestamp());
    }
}
