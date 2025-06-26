package org.common.kafka.payloads;

public class CreateCatPayload implements CatOperationPayload {
    private Long chatId;
    private String name;
    private byte[] photoData;

    public CreateCatPayload() {}

    public CreateCatPayload(Long chatId, String name, byte[] photoData) {
        this.chatId = chatId;
        this.name = name;
        this.photoData = photoData;
    }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public byte[] getPhotoData() { return photoData; }
    public void setPhotoData(byte[] photoData) { this.photoData = photoData; }
} 