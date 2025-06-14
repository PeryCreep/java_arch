package org.javaEnterprise.services;

import org.javaEnterprise.handlers.states.UserState;
import org.springframework.stereotype.Service;

@Service
public class UserDataFacade {
    
    private final UserStateService userStateService;
    private final UserTempDataService userTempDataService;
    
    public UserDataFacade(UserStateService userStateService, UserTempDataService userTempDataService) {
        this.userStateService = userStateService;
        this.userTempDataService = userTempDataService;
    }

    public UserState getCurrentState(Long chatId) {
        return userStateService.getCurrentState(chatId);
    }

    public void setState(Long chatId, UserState state) {
        userStateService.setState(chatId, state);
    }


    public void storeTextData(Long chatId, String key, String value) {
        userTempDataService.storeTextData(chatId, key, value);
    }

    public String getTextData(Long chatId, String key) {
        return userTempDataService.getTextData(chatId, key);
    }


    public void storePage(Long chatId, Integer page) {
        userTempDataService.storePage(chatId, page);
    }

    public Integer getPage(Long chatId) {
        return userTempDataService.getPage(chatId);
    }

    public void storeFormData(Long chatId, String key, Object value) {
        userTempDataService.storeFormData(chatId, key, value);
    }

    public <T> T getFormData(Long chatId, String key, Class<T> clazz) {
        return userTempDataService.getFormData(chatId, key, clazz);
    }

    public void clearFormData(Long chatId, String key) {
        userTempDataService.clearFormData(chatId, key);
    }

} 