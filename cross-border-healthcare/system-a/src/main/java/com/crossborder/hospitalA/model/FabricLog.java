package com.crossborder.hospitalA.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fabric_logs")
public class FabricLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String doctorId;
    private String doctorName;
    private String patientId;
    private String status;
    private String timestamp;

    // New Fabric Transaction Details
    private String transactionId;
    private Long blockNumber;
    private String validationCode;

    @Column(length = 5000)
    private String responsePayload;

    @Column(length = 5000)
    private String inputArgs;  // JSON array as string

    @Column(length = 5000)
    private String endorsers;  // JSON array as string

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }

    public String getValidationCode() { return validationCode; }
    public void setValidationCode(String validationCode) { this.validationCode = validationCode; }

    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }

    public String getInputArgs() { return inputArgs; }
    public void setInputArgs(String inputArgs) { this.inputArgs = inputArgs; }

    public String getEndorsers() { return endorsers; }
    public void setEndorsers(String endorsers) { this.endorsers = endorsers; }
}
