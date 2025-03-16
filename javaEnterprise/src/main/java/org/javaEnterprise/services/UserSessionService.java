package org.javaEnterprise.services;

import org.javaEnterprise.handlers.states.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<String, String> userData = new ConcurrentHashMap<>();
    private final Set<Long> initializedUsers = ConcurrentHashMap.newKeySet();

    public UserState getCurrentState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.START);
    }

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public void storeTempData(Long chatId, String key, String value) {
        userData.put(chatId + "_" + key, value);
    }

    public String getTempData(Long chatId, String key) {
        return userData.get(chatId + "_" + key);
    }

    public boolean isUserInitialized(Long chatId) {
        return initializedUsers.contains(chatId);
    }

    public void markUserAsInitialized(Long chatId) {
        initializedUsers.add(chatId);
    }
}
