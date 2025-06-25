package org.common.kafka.payloads;

import org.common.domain.Cat;

import java.util.List;

public class CatListResponsePayload implements CatResponsePayload {
    private List<Cat> cats;
    private int page;
    private int size;


    public CatListResponsePayload() {}
    public CatListResponsePayload(List<Cat> cats,int page, int size) {
        this.cats = cats;
        this.page = page;
        this.size = size;
    }
    public List<Cat> getCats() { return cats; }
    public void setCats(List<Cat> cats) { this.cats = cats; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
} 