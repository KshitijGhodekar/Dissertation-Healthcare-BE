package com.crossborder.hospitalA.model;

public class FabricResponse {
    private String transactionId;
    private long blockNumber;
    private String validationCode;
    private String responsePayload;
    private String inputArgsJson;
    private String endorsersJson;
    private boolean accessGranted;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(long blockNumber) { this.blockNumber = blockNumber; }

    public String getValidationCode() { return validationCode; }
    public void setValidationCode(String validationCode) { this.validationCode = validationCode; }

    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }

    public String getInputArgsJson() { return inputArgsJson; }
    public void setInputArgsJson(String inputArgsJson) { this.inputArgsJson = inputArgsJson; }

    public String getEndorsersJson() { return endorsersJson; }
    public void setEndorsersJson(String endorsersJson) { this.endorsersJson = endorsersJson; }

    public boolean isAccessGranted() { return accessGranted; }
    public void setAccessGranted(boolean accessGranted) { this.accessGranted = accessGranted; }
}
