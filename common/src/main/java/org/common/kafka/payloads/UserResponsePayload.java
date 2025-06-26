package org.common.kafka.payloads;

import org.common.domain.User;

public class UserResponsePayload implements CatResponsePayload {
    private User user;
    public UserResponsePayload() {}
    public UserResponsePayload(User user) { this.user = user; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
} 