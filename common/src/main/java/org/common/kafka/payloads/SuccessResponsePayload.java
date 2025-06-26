package org.common.kafka.payloads;

public class SuccessResponsePayload implements CatResponsePayload {
    private String message;

    public SuccessResponsePayload() {}
    public SuccessResponsePayload(String message) { this.message = message; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
} 