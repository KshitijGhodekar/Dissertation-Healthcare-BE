package com.crossborder.hospitalB.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendAccessAlert(String to, String doctorId, String patientId, boolean granted) {
        String subject = granted ? "✅ Access Granted" : "❌ Access Denied";
        String body = "Doctor " + doctorId + " tried to access patient " + patientId + ". Access " +
                (granted ? "was approved." : "was denied.");

        System.out.println("📧 Sending email to: " + to); // 👈 Add this
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("kshitijdghodekar@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
