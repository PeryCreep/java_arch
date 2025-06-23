package org.catService.domain;

public class CatRating {

    private Long id;
    private User user;
    private Cat cat;
    private boolean like;


    public CatRating(User user, Cat cat, boolean like) {
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCat(Cat cat) {
        this.cat = cat;
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