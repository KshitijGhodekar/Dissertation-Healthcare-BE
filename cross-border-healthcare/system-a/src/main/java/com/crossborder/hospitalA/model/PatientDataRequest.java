package com.crossborder.hospitalA.model;

import java.io.Serializable;

public class PatientDataRequest implements Serializable {
    private String doctorId;
    private String patientId;
    private String timestamp;
    private String purpose;
    private String hospitalName;  // ✅ Added field

    public PatientDataRequest() {}

    public PatientDataRequest(String doctorId, String patientId, String timestamp, String purpose, String hospitalName) {
        this.doctorId = doctorId;
        this.patientId = patientId;
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

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    @Override
    public String toString() {
        return "PatientDataRequest{" +
                "doctorId='" + doctorId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", purpose='" + purpose + '\'' +
                ", hospitalName='" + hospitalName + '\'' +
                '}';
    }
}
