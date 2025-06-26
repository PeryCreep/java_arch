package org.common.kafka.payloads;

public class IsUserExistsPayload implements CatOperationPayload {
    private Long chatId;
    public IsUserExistsPayload() {}
    public IsUserExistsPayload(Long chatId) { this.chatId = chatId; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
} 