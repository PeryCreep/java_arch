package org.common.kafka.payloads;

public class RateCatPayload implements CatOperationPayload {
    private Long catId;
    private Long userId;
    private boolean isLike;

    public RateCatPayload() {}

    public RateCatPayload(Long catId, Long userId, boolean isLike) {
        this.catId = catId;
        this.userId = userId;
        this.isLike = isLike;
    }

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isLike() { return isLike; }
    public void setLike(boolean isLike) { this.isLike = isLike; }
} 