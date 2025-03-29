package org.javaEnterprise.domain;

import jakarta.persistence.*;


@Entity
@Table(name = "cat_ratings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "cat_id"}))
public class CatRating {

    public CatRating() {
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "cat_id", nullable = false)
    private Cat cat;

    @Column(name = "is_like", nullable = false)
    private boolean like;


    public CatRating(User user, Cat cat, boolean like) {
        this.user = user;
        this.cat = cat;
        this.like = like;
    }

    public User getUser() {
        return user;
    }

    public Cat getCat() {
        return cat;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }
}