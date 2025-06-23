package org.common.kafka.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.common.kafka.payloads.*;

import java.io.Serializable;

public class CatResponseMessage implements Serializable {
    private Long requestId;
    private String status;
    private String message;
    private CatResponsePayload payload;

    public CatResponseMessage() {}

    public CatResponseMessage(Long requestId, String status, String message, CatResponsePayload payload) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
        this.payload = payload;
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public CatResponsePayload getPayload() { return payload; }
    public void setPayload(CatResponsePayload payload) { this.payload = payload; }
} 