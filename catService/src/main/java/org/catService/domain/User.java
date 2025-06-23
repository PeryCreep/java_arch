package org.catService.domain;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private Long chatId;
    private String name;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Long chatId, String name, LocalDateTime createdAt) {
        this.chatId = chatId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public User(Long id,Long chatId, String name, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
