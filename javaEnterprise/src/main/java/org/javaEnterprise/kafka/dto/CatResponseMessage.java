package org.javaEnterprise.kafka.dto;

import java.io.Serializable;
import java.util.Map;

public class CatResponseMessage implements Serializable {
    private Long requestId; // Для сопоставления с запросом
    private String status; // OK, ERROR
    private String message; // Сообщение об ошибке или статусе
    private Map<String, Object> payload; // Данные ответа (например, коты)

    public CatResponseMessage() {}

    public CatResponseMessage(Long requestId, String status, String message, Map<String, Object> payload) {
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
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
} 