package org.common.kafka.payloads;

import org.common.domain.Cat;

public class SingleCatResponsePayload implements CatResponsePayload {
    private Cat cat;

    public SingleCatResponsePayload() {}
    public SingleCatResponsePayload(Cat cat) { this.cat = cat; }
    public Cat getCat() { return cat; }
    public void setCat(Cat cat) { this.cat = cat; }
} 