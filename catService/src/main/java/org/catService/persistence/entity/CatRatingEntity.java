package org.catService.persistence.entity;


import jakarta.persistence.*;
import org.common.domain.CatRating;
import org.catService.persistence.entity.CatEntity;
import org.catService.persistence.entity.UserEntity;


@Entity
@Table(name = "cat_ratings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cat_id"}))
public class CatRatingEntity {

    public CatRatingEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "cat_id", nullable = false)
    private CatEntity cat;

    @Column(name = "is_like", nullable = false)
    private boolean like;


    public CatRatingEntity(UserEntity user, CatEntity cat, boolean like) {
        this.user = user;
        this.cat = cat;
        this.like = like;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public CatEntity getCat() {
        return cat;
    }

    public void setCat(CatEntity cat) {
        this.cat = cat;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }
}