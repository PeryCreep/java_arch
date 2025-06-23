package org.common.kafka.payloads;

public class GetMyCatsPayload implements CatOperationPayload {
    private Long authorId;
    private int page;
    private int size;

    public GetMyCatsPayload() {}
    public GetMyCatsPayload(Long authorId, int page, int size) {
        this.authorId = authorId;
        this.page = page;
        this.size = size;
    }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
} 