package org.common.kafka.payloads;

public class FindUserByChatIdPayload implements CatOperationPayload {
    private Long chatId;
    public FindUserByChatIdPayload() {}
    public FindUserByChatIdPayload(Long chatId) { this.chatId = chatId; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
} 