package org.javaEnterprise.services;

import org.javaEnterprise.domain.User;
import org.javaEnterprise.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(Long chatId, String name) {
        User user = new User();
        user.setChatId(chatId);
        user.setName(name);
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User saveUserName(Long chatId, String name) {
        Optional<User> userOpt = findByChatId(chatId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(name);
            return userRepository.save(user);
        } else {
            return createUser(chatId, name);
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> findByChatId(Long chatId) {
        try {
            return userRepository.findByChatId(chatId);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public boolean isUserExists(Long chatId) {
        return userRepository.existsByChatId(chatId);
    }
} 