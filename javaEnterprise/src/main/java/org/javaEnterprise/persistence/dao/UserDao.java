package org.javaEnterprise.persistence.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.domain.repository.UserRepository;
import org.javaEnterprise.persistence.entity.UserEntity;
import org.javaEnterprise.persistence.mapper.EntityDomainMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
public class UserDao implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final EntityDomainMapper mapper;

    public UserDao(EntityDomainMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        try {
            UserEntity entity = mapper.userToEntity(user);
            
            if (entity.getId() != null) {
                entity = entityManager.merge(entity);
            } else {
                entityManager.persist(entity);
            }
            
            entityManager.flush();
            return mapper.userToDomain(entity);
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        try {
            List<UserEntity> entities = entityManager
                    .createQuery("SELECT u FROM UserEntity u", UserEntity.class)
                    .getResultList();
            
            return entities.stream()
                    .map(mapper::userToDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error finding all users: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        try {
            UserEntity entity = entityManager.find(UserEntity.class, id);
            return Optional.ofNullable(entity).map(mapper::userToDomain);
        } catch (Exception e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByChatId(Long chatId) {
        try {
            TypedQuery<UserEntity> query = entityManager
                    .createQuery("SELECT u FROM UserEntity u WHERE u.chatId = :chatId", UserEntity.class)
                    .setParameter("chatId", chatId);
            
            UserEntity entity = query.getSingleResult();
            return Optional.ofNullable(entity).map(mapper::userToDomain);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error finding user by chatId: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByChatId(Long chatId) {
        try {
            Long count = entityManager
                    .createQuery("SELECT COUNT(u) FROM UserEntity u WHERE u.chatId = :chatId", Long.class)
                    .setParameter("chatId", chatId)
                    .getSingleResult();
            
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        try {
            UserEntity entity = entityManager.find(UserEntity.class, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }
} 