package org.javaEnterprise.persistence.mapper;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.CatRating;
import org.javaEnterprise.domain.User;

import org.javaEnterprise.persistence.entity.CatEntity;
import org.javaEnterprise.persistence.entity.CatRatingEntity;
import org.javaEnterprise.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;


@Component
public class EntityDomainMapper {

    public UserEntity userToEntity(User domain) {
        if (domain == null) {
            return null;
        }
        
        UserEntity entity = new UserEntity();
        entity.setChatId(domain.getChatId());
        entity.setName(domain.getName());
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        return entity;
    }

    public User userToDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new User(
            entity.getId(),
            entity.getChatId(),
            entity.getName(),
            entity.getCreatedAt()
        );
    }

    public CatEntity catToEntity(Cat domain) {
        if (domain == null) {
            return null;
        }
        
        CatEntity entity = new CatEntity();

        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        
        entity.setName(domain.getName());
        entity.setPhotoData(domain.getPhotoData());
        entity.setLikesCount(domain.getLikesCount());
        entity.setDislikesCount(domain.getDislikesCount());

        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }

        User author = domain.getAuthor();
        if (author != null) {
            UserEntity authorEntity = userToEntity(author);
            if (author.getId() != null) {
                authorEntity.setId(author.getId());
            }
            entity.setAuthor(authorEntity);
        }
        
        return entity;
    }

    public Cat catToDomain(CatEntity entity) {
        if (entity == null) {
            return null;
        }
        
        User author = userToDomain(entity.getAuthor());
        
        return new Cat(
            entity.getId(),
            entity.getName(),
            entity.getPhotoData(),
            author,
            entity.getLikesCount(),
            entity.getDislikesCount(),
            entity.getCreatedAt()
        );
    }

    public CatRating catRatingToDomain(CatRatingEntity entity) {
        if (entity == null) {
            return null;
        }
        
        User user = userToDomain(entity.getUser());
        Cat cat = catToDomain(entity.getCat());
        
        CatRating rating = new CatRating(user, cat, entity.isLike());
        rating.setId(entity.getId());
        return rating;
    }
} 