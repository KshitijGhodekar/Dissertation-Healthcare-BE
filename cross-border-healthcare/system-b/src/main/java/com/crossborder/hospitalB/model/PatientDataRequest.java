package com.crossborder.hospitalB.model;

import java.io.Serializable;
import java.util.List;

public class PatientDataRequest implements Serializable {
    private String doctorId;
    private String doctorName;
    private String patientId;
    private List<String> patientIds;
    private String timestamp;
    private String purpose;
    private String hospitalName;

    public PatientDataRequest() {}

    public PatientDataRequest(String doctorId, String doctorName, String patientId, List<String> patientIds, String timestamp, String purpose, String hospitalName) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientIds = patientIds;
        this.timestamp = timestamp;
        this.purpose = purpose;
        this.hospitalName = hospitalName;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public List<String> getPatientIds() {
        return patientIds;
    }

    public void setPatientIds(List<String> patientIds) {
        this.patientIds = patientIds;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
}
