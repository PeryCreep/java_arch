package org.common.kafka.payloads;

public class GetCatByIdPayload implements CatOperationPayload {
    private Long catId;

    public GetCatByIdPayload() {}
    public GetCatByIdPayload(Long catId) { this.catId = catId; }
    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }
} 