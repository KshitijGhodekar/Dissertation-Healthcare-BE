package com.crossborder.hospitalB.controller;

import com.crossborder.hospitalB.model.PatientDataRequest;
import com.crossborder.hospitalB.producer.PatientRequestKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(PatientRequestController.class)
class PatientRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientRequestKafkaProducer producer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testRequestPatientData_Success() throws Exception {
        PatientDataRequest request = new PatientDataRequest(
                "D001", "Dr. Smith", "P001", List.of("P001"),
                "2025-07-25T10:00:00", "Diagnosis", "Ireland-Hospital", "REQ123"
        );

        mockMvc.perform(post("/api/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Request sent successfully."));
    }

    @Test
    void testRequestPatientData_ExceptionThrown() throws Exception {
        PatientDataRequest request = new PatientDataRequest(
                "D002", "Dr. Error", "P999", List.of("P999"),
                "2025-07-25T12:00:00", "Test", "Test-Hospital", "REQ999"
        );

        Mockito.doThrow(new RuntimeException("Kafka failure"))
                .when(producer).sendPatientDataRequest(Mockito.any());

        mockMvc.perform(post("/api/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to send request")));
    }
}
