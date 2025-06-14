package org.javaEnterprise.persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Query;
import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.domain.repository.CatRepository;
import org.javaEnterprise.persistence.entity.CatEntity;
import org.javaEnterprise.persistence.entity.UserEntity;
import org.javaEnterprise.persistence.mapper.EntityDomainMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CatDao implements CatRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final EntityDomainMapper mapper;
    private final UserDao userDao;

    public CatDao(EntityDomainMapper mapper, UserDao userDao) {
        this.mapper = mapper;
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public Cat save(Cat cat) {
        try {
            User author = cat.getAuthor();
            if (author != null) {
                if (author.getId() == null) {
                    author = userDao.save(author);
                    cat.setAuthor(author);
                } else {
                    boolean exists = entityManager.find(UserEntity.class, author.getId()) != null;
                    if (!exists) {
                        author = userDao.save(author);
                        cat.setAuthor(author);
                    }
                }
            }

            boolean isUpdate = cat.getId() != null;
            CatEntity existingEntity = null;
            
            if (isUpdate) {
                existingEntity = entityManager.find(CatEntity.class, cat.getId());
                if (existingEntity == null) {
                    throw new IllegalArgumentException("Cat with ID " + cat.getId() + " does not exist");
                }
            }

            CatEntity entity;
            if (isUpdate && existingEntity != null) {
                existingEntity.setName(cat.getName());
                existingEntity.setLikesCount(cat.getLikesCount());
                existingEntity.setDislikesCount(cat.getDislikesCount());

                if (author != null) {
                    UserEntity authorEntity = entityManager.getReference(UserEntity.class, author.getId());
                    existingEntity.setAuthor(authorEntity);
                }
                entity = entityManager.merge(existingEntity);
            } else {
                entity = mapper.catToEntity(cat);

                if (author != null && author.getId() != null) {
                    UserEntity authorEntity = entityManager.getReference(UserEntity.class, author.getId());
                    entity.setAuthor(authorEntity);
                }
                
                entityManager.persist(entity);
            }
            
            entityManager.flush();
            return mapper.catToDomain(entity);
        } catch (Exception e) {
            System.err.println("Error saving cat: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save cat: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cat> findAll() {
        try {
            List<CatEntity> entities = entityManager
                    .createQuery("SELECT c FROM CatEntity c", CatEntity.class)
                    .getResultList();
            
            return entities.stream()
                    .map(mapper::catToDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error finding all cats: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cat> findById(Long id) {
        try {
            CatEntity entity = entityManager.find(CatEntity.class, id);
            return Optional.ofNullable(entity).map(mapper::catToDomain);
        } catch (Exception e) {
            System.err.println("Error finding cat by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Cat> findRandomCat() {
        try {
            TypedQuery<CatEntity> query = entityManager.createQuery(
                    "SELECT c FROM CatEntity c ORDER BY FUNCTION('RAND')", 
                    CatEntity.class);
            query.setMaxResults(1);
            
            List<CatEntity> results = query.getResultList();
            if (!results.isEmpty()) {
                return Optional.of(mapper.catToDomain(results.get(0)));
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error finding random cat: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Cat> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable) {
        try {
            TypedQuery<CatEntity> query = entityManager.createQuery(
                    "SELECT c FROM CatEntity c WHERE c.author.id = :authorId ORDER BY c.createdAt DESC",
                    CatEntity.class);
            query.setParameter("authorId", authorId);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
            
            List<CatEntity> results = query.getResultList();

            TypedQuery<Long> countQuery = entityManager.createQuery(
                    "SELECT COUNT(c) FROM CatEntity c WHERE c.author.id = :authorId",
                    Long.class);
            countQuery.setParameter("authorId", authorId);
            Long total = countQuery.getSingleResult();

            List<Cat> cats = results.stream()
                    .map(mapper::catToDomain)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(cats, pageable, total);
        } catch (Exception e) {
            System.err.println("Error finding cats by author: " + e.getMessage());
            return Page.empty();
        }
    }

    @Override
    @Transactional
    public int deleteByIdAndAuthorId(Long catId, Long authorId) {
        try {
            Query query = entityManager.createQuery(
                    "DELETE FROM CatEntity c WHERE c.id = :catId AND c.author.id = :authorId");
            query.setParameter("catId", catId);
            query.setParameter("authorId", authorId);
            
            return query.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error deleting cat: " + e.getMessage());
            return 0;
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        try {
            CatEntity entity = entityManager.find(CatEntity.class, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
        } catch (Exception e) {
            System.err.println("Error deleting cat: " + e.getMessage());
            throw new RuntimeException("Failed to delete cat", e);
        }
    }
} 