package org.catService.persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.common.domain.Cat;
import org.common.domain.CatRating;
import org.common.domain.User;
import org.common.domain.repository.CatRatingRepository;
import org.catService.persistence.entity.CatEntity;
import org.catService.persistence.entity.CatRatingEntity;
import org.catService.persistence.entity.UserEntity;
import org.catService.persistence.mapper.EntityDomainMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CatRatingDao implements CatRatingRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final EntityDomainMapper mapper;

    public CatRatingDao(EntityDomainMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CatRating> findByCatAndUser(Cat cat, User user) {
        try {
            TypedQuery<CatRatingEntity> query = entityManager.createQuery(
                "SELECT r FROM CatRatingEntity r " +
                "WHERE r.cat.id = :catId AND r.user.id = :userId",
                CatRatingEntity.class
            );
            query.setParameter("catId", cat.getId());
            query.setParameter("userId", user.getId());
            
            CatRatingEntity entity = query.getSingleResult();
            return Optional.of(mapper.catRatingToDomain(entity));
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error finding rating by cat and user: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCatAndLikeStatus(Cat cat, boolean likeStatus) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(r) FROM CatRatingEntity r " +
                "WHERE r.cat.id = :catId AND r.like = :likeStatus",
                Long.class
            );
            query.setParameter("catId", cat.getId());
            query.setParameter("likeStatus", likeStatus);
            
            return query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Error counting ratings by cat and like status: " + e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional
    public CatRating save(CatRating rating) {
        try {
            User user = rating.getUser();
            Cat cat = rating.getCat();
            
            if (user == null || user.getId() == null) {
                throw new IllegalArgumentException("User must be saved before rating a cat");
            }
            
            if (cat == null || cat.getId() == null) {
                throw new IllegalArgumentException("Cat must be saved before being rated");
            }

            Optional<CatRatingEntity> existingRating = findRatingEntity(cat.getId(), user.getId());
            
            CatRatingEntity entity;
            if (existingRating.isPresent()) {
                entity = existingRating.get();
                entity.setLike(rating.isLike());
                entityManager.merge(entity);
            } else {
                entity = new CatRatingEntity();

                UserEntity userEntity = entityManager.getReference(UserEntity.class, user.getId());
                CatEntity catEntity = entityManager.getReference(CatEntity.class, cat.getId());
                
                entity.setUser(userEntity);
                entity.setCat(catEntity);
                entity.setLike(rating.isLike());
                
                entityManager.persist(entity);
            }
            
            entityManager.flush();
            return mapper.catRatingToDomain(entity);
            
        } catch (Exception e) {
            System.err.println("Error saving cat rating: " + e.getMessage());
            throw new RuntimeException("Failed to save cat rating", e);
        }
    }

    public List<CatRating> listByCat(Cat cat) {
        return entityManager.createQuery(
                        "SELECT cr FROM CatRatingEntity cr WHERE cr.cat.id = :catId",
                        CatRatingEntity.class)
                .setParameter("catId", cat.getId())
                .getResultList().stream()
                .map(mapper::catRatingToDomain)
                .toList();
    }

    private Optional<CatRatingEntity> findRatingEntity(Long catId, Long userId) {
        try {
            TypedQuery<CatRatingEntity> query = entityManager.createQuery(
                "SELECT r FROM CatRatingEntity r " +
                "WHERE r.cat.id = :catId AND r.user.id = :userId",
                CatRatingEntity.class
            );
            query.setParameter("catId", catId);
            query.setParameter("userId", userId);
            
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error finding rating entity: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void deleteAll(Long catId) {
        try {

            entityManager.createQuery(
                "DELETE FROM CatRatingEntity r WHERE r.cat.id = :catId"
            )
            .setParameter("catId", catId)
            .executeUpdate();
            
        } catch (Exception e) {
            System.err.println("Error deleting all cat ratings: " + e.getMessage());
            throw new RuntimeException("Failed to delete cat ratings", e);
        }
    }
} 