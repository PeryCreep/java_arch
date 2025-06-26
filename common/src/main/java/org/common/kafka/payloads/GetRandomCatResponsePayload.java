package org.common.kafka.payloads;

import org.common.domain.Cat;

public class GetRandomCatResponsePayload implements CatResponsePayload {
    private Cat cat;
    private Long likeCount;
    private Long dislikeCount;

    public GetRandomCatResponsePayload() {}
    public GetRandomCatResponsePayload(Cat cat, Long likeCount, Long dislikeCount) {
        this.cat = cat;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }
    public Cat getCat() { return this.cat; }
    public void setCat(Cat cat) { this.cat = cat; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Long getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Long dislikeCount) { this.dislikeCount = dislikeCount; }
}