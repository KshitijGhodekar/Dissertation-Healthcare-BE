package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.model.PatientEntity;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Mock
    private MimeMessage mimeMessage;

    @Captor
    ArgumentCaptor<MimeMessage> mimeCaptor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendPatientDataPDF_Success() {
        PatientEntity patient = new PatientEntity();
        patient.setPatientId("P001");
        patient.setName("John Doe");
        patient.setAge(30);
        patient.setGender("Male");
        patient.setBloodType("A+");
        patient.setMedicalCondition("Diabetes");
        patient.setMedication("Metformin");
        patient.setTestResults("Stable");
        patient.setDoctor("Dr. Smith");
        patient.setHospital("Test Hospital");
        patient.setDateOfAdmission(LocalDate.of(2025, 1, 1));
        patient.setDischargeDate(LocalDate.of(2025, 1, 10));

        emailNotificationService.sendPatientDataPDF(
                "test@example.com",
                "Dr. Smith",
                patient,
                true
        );

        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}
