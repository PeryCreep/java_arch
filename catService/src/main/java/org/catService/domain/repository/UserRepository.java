package org.catService.domain.repository;

import org.catService.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByChatId(Long chatId);
    boolean existsByChatId(Long chatId);
    void deleteById(Long id);
} 