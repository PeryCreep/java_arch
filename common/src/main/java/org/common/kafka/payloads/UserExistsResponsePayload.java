package org.common.kafka.payloads;

public class UserExistsResponsePayload implements CatResponsePayload {
    private boolean exists;
    public UserExistsResponsePayload() {}
    public UserExistsResponsePayload(boolean exists) { this.exists = exists; }
    public boolean isExists() { return exists; }
    public void setExists(boolean exists) { this.exists = exists; }
} 