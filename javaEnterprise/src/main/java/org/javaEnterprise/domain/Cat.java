package org.javaEnterprise.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cat {
    private Long id;
    private String name;
    private byte[] photoData;
    private User author;
    private Integer likesCount;
    private Integer dislikesCount;
    private LocalDateTime createdAt;

    private List<CatRating> ratings = new ArrayList<>();

    public Cat() {
        this.likesCount = 0;
        this.dislikesCount = 0;
    }

    public Cat(Long id, String name, byte[] photoData, User author, Integer likesCount, 
               Integer dislikesCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.photoData = photoData;
        this.author = author;
        this.likesCount = likesCount != null ? likesCount : 0;
        this.dislikesCount = dislikesCount != null ? dislikesCount : 0;
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

    public Integer getLikesCount() {
        return likesCount;
    }

    public Integer getDislikesCount() {
        return dislikesCount;
    }

    public List<CatRating> getRatings() {
        return ratings;
    }

    public void incrementLikes() {
        this.likesCount++;
    }

    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    public void incrementDislikes() {
        this.dislikesCount++;
    }

    public void decrementDislikes() {
        if (this.dislikesCount > 0) {
            this.dislikesCount--;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
