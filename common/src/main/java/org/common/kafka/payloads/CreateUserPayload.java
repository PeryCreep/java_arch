package org.common.kafka.payloads;

public class CreateUserPayload implements CatOperationPayload {
    private Long chatId;
    private String name;
    public CreateUserPayload() {}
    public CreateUserPayload(Long chatId, String name) { this.chatId = chatId; this.name = name; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
} 