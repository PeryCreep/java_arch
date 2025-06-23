package org.javaEnterprise.kafka.dto;

import java.io.Serializable;
import java.util.Map;

public class CatRequestMessage implements Serializable {
    private String operation; // Например: CREATE_CAT, GET_MY_CATS, GET_RANDOM_CAT
    private Map<String, Object> payload; // Данные для операции
    private Long requestId; // Для сопоставления ответа
    private Long userId; // id пользователя (chatId)

    public CatRequestMessage() {}

    public CatRequestMessage(String operation, Map<String, Object> payload, Long requestId, Long userId) {
        this.operation = operation;
        this.payload = payload;
        this.requestId = requestId;
        this.userId = userId;
    }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
} 