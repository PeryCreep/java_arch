package org.common.kafka.payloads;

public class ErrorResponsePayload implements CatResponsePayload {
    private String error;

    public ErrorResponsePayload() {}
    public ErrorResponsePayload(String error) { this.error = error; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
} 