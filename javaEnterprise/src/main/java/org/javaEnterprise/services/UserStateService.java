package org.javaEnterprise.services;

import org.javaEnterprise.handlers.states.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {
    
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public UserState getCurrentState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.START);
    }

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

} 