package org.common.kafka.payloads;

public class DeleteCatPayload implements CatOperationPayload {
    private Long catId;
    private Long chatId;

    public DeleteCatPayload() {}
    public DeleteCatPayload(Long catId, Long chatId) {
        this.catId = catId;
        this.chatId = chatId;
    }
    public Long getCatId() { return catId; }
    public Long getChatId() { return chatId; }
} 