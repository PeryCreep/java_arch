package org.common.kafka.payloads;

import org.common.domain.Cat;

import java.util.List;

public class CatListResponsePayload implements CatResponsePayload {
    private List<Cat> cats;

    public CatListResponsePayload() {}
    public CatListResponsePayload(List<Cat> cats) { this.cats = cats; }
    public List<Cat> getCats() { return cats; }
    public void setCats(List<Cat> cats) { this.cats = cats; }
} 