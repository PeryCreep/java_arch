package org.javaEnterprise.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cat {
    private Long id;
    private String name;
    private byte[] photoData;
    private User author;
    private LocalDateTime createdAt;

    public Cat() {
    }

    public Cat(Long id, String name, byte[] photoData, User author, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.photoData = photoData;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
