package org.common.kafka.payloads;

public class UpdateCatPayload implements CatOperationPayload {
    private Long catId;
    private String name;
    private byte[] photoData;

    public UpdateCatPayload() {}
    public UpdateCatPayload(Long catId, String name, byte[] photoData) {
        this.catId = catId;
        this.name = name;
        this.photoData = photoData;
    }
    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public byte[] getPhotoData() { return photoData; }
    public void setPhotoData(byte[] photoData) { this.photoData = photoData; }
} 