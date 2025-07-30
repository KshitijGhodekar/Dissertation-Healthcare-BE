package com.crossborder.hospitalA.model;

import java.io.Serializable;
import java.util.List;

public class PatientDataRequest implements Serializable {

    private String requestId;
    private String doctorId;
    private String doctorName;
    private String patientId;
    private List<String> patientIds;
    private String mobileNumber;
    private String timestamp;
    private String purpose;
    private String hospitalName;

    public PatientDataRequest() {}

    public PatientDataRequest(String requestId, String doctorId, String doctorName, String patientId,
                              String timestamp, String purpose, String hospitalName) {
        this.requestId = requestId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.timestamp = timestamp;
        this.purpose = purpose;
        this.hospitalName = hospitalName;
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public List<String> getPatientIds() { return patientIds; }
    public void setPatientIds(List<String> patientIds) { this.patientIds = patientIds; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    @Override
    public String toString() {
        return "PatientDataRequest{" +
                "requestId='" + requestId + '\'' +
                ", doctorId='" + doctorId + '\'' +
                ", doctorName='" + doctorName + '\'' +
                ", patientId='" + patientId + '\'' +
                ", patientIds=" + patientIds +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", purpose='" + purpose + '\'' +
                ", hospitalName='" + hospitalName + '\'' +
                '}';
    }
}
