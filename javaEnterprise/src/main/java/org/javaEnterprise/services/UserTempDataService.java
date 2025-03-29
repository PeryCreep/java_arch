package org.javaEnterprise.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserTempDataService {
    
    // Коллекция для хранения текстовых данных
    private final Map<Long, Map<String, String>> userTextData = new ConcurrentHashMap<>();
    
    // Коллекция для хранения числовых данных
    private final Map<Long, Map<String, Integer>> userNumericData = new ConcurrentHashMap<>();
    
    // Коллекция для хранения данных страниц (для пагинации)
    private final Map<Long, Integer> userPageData = new ConcurrentHashMap<>();
    
    // Коллекция для хранения идентификаторов (ID) сущностей
    private final Map<Long, Map<String, Long>> userEntityIds = new ConcurrentHashMap<>();
    
    // Коллекция для хранения данных состояний
    private final Map<Long, Map<String, Object>> userFormData = new ConcurrentHashMap<>();

    public void storeTextData(Long chatId, String key, String value) {
        userTextData
                .computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                .put(key, value);
    }

    public String getTextData(Long chatId, String key) {
        Map<String, String> userData = userTextData.get(chatId);
        return userData != null ? userData.get(key) : null;
    }

    public void storePage(Long chatId, Integer page) {
        userPageData.put(chatId, page);
    }

    public Integer getPage(Long chatId) {
        return userPageData.getOrDefault(chatId, 0);
    }

    public void storeFormData(Long chatId, String key, Object value) {
        userFormData
                .computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                .put(key, value);
    }
    

    public <T> T getFormData(Long chatId, String key, Class<T> clazz) {
        Map<String, Object> userData = userFormData.get(chatId);
        if (userData == null || !userData.containsKey(key)) {
            return null;
        }
        
        try {
            return clazz.cast(userData.get(key));
        } catch (ClassCastException e) {
            return null;
        }
    }

    public void clearFormData(Long chatId, String key) {
        Map<String, Object> userData = userFormData.get(chatId);
        if (userData != null) {
            userData.remove(key);
        }
    }

} 