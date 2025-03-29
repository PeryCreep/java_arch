package org.javaEnterprise.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cats")
public class Cat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(name = "photo_data", nullable = false)
    private byte[] photoData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "dislikes_count", nullable = false)
    private Integer dislikesCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "cat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CatRating> ratings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    /**
     * Увеличивает счетчик лайков
     */
    public void incrementLikes() {
        this.likesCount++;
    }
    
    /**
     * Уменьшает счетчик лайков
     */
    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }
    
    /**
     * Увеличивает счетчик дизлайков
     */
    public void incrementDislikes() {
        this.dislikesCount++;
    }
    
    /**
     * Уменьшает счетчик дизлайков
     */
    public void decrementDislikes() {
        if (this.dislikesCount > 0) {
            this.dislikesCount--;
        }
    }
}
