package org.common.kafka.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.common.kafka.payloads.*;
import java.io.Serializable;

public class CatRequestMessage implements Serializable {
    private CatOperationType operation;
    private CatOperationPayload payload;
    private Long requestId;
    private Long userId;

    public CatRequestMessage() {}

    public CatRequestMessage(CatOperationType operation, CatOperationPayload payload, Long requestId, Long userId) {
        this.operation = operation;
        this.payload = payload;
        this.requestId = requestId;
        this.userId = userId;
    }

    public CatOperationType getOperation() { return operation; }
    public void setOperation(CatOperationType operation) { this.operation = operation; }
    public CatOperationPayload getPayload() { return payload; }
    public void setPayload(CatOperationPayload payload) { this.payload = payload; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
} 