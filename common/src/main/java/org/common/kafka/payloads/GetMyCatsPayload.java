package org.common.kafka.payloads;

public class GetMyCatsPayload implements CatOperationPayload {
    private Long chatId;
    private int page;
    private int size;

    public GetMyCatsPayload() {}
    public GetMyCatsPayload(Long chatId, int page, int size) {
        this.chatId = chatId;
        this.page = page;
        this.size = size;
    }
    public Long getChatId() { return chatId; }
    public void setChatId(Long authorId) { this.chatId = authorId; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
} 