package org.common.domain.repository;

import org.common.domain.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByChatId(Long chatId);
    public boolean existsByChatId(Long chatId);
} 