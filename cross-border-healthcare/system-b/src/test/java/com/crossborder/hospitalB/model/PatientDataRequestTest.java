package com.crossborder.hospitalB.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientDataRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        PatientDataRequest request = new PatientDataRequest();

        request.setDoctorId("D001");
        request.setDoctorName("Dr. John");
        request.setPatientId("P001");
        request.setPatientIds(Arrays.asList("P001", "P002"));
        request.setTimestamp("2025-07-25T12:00:00");
        request.setPurpose("Treatment");
        request.setHospitalName("General Hospital");
        request.setRequestId("REQ123");

        assertEquals("D001", request.getDoctorId());
        assertEquals("Dr. John", request.getDoctorName());
        assertEquals("P001", request.getPatientId());
        assertEquals(Arrays.asList("P001", "P002"), request.getPatientIds());
        assertEquals("2025-07-25T12:00:00", request.getTimestamp());
        assertEquals("Treatment", request.getPurpose());
        assertEquals("General Hospital", request.getHospitalName());
        assertEquals("REQ123", request.getRequestId());
    }

    @Test
    void testAllArgsConstructor() {
        List<String> ids = Arrays.asList("P001", "P002");

        PatientDataRequest request = new PatientDataRequest(
                "D002", "Dr. Alice", "P003", ids,
                "2025-07-25T15:00:00", "Diagnosis", "City Hospital", "REQ456"
        );

        assertEquals("D002", request.getDoctorId());
        assertEquals("Dr. Alice", request.getDoctorName());
        assertEquals("P003", request.getPatientId());
        assertEquals(ids, request.getPatientIds());
        assertEquals("2025-07-25T15:00:00", request.getTimestamp());
        assertEquals("Diagnosis", request.getPurpose());
        assertEquals("City Hospital", request.getHospitalName());
        assertEquals("REQ456", request.getRequestId());
    }
}
