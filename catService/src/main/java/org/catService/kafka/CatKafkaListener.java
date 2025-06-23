package org.catService.kafka;

import org.catService.kafka.dto.CatRequestMessage;
import org.catService.kafka.dto.CatResponseMessage;
import org.catService.services.CatService;
import org.catService.domain.Cat;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

@Component
public class CatKafkaListener {
    private final CatService catService;
    private final KafkaTemplate<String, CatResponseMessage> kafkaTemplate;

    public CatKafkaListener(CatService catService, KafkaTemplate<String, CatResponseMessage> kafkaTemplate) {
        this.catService = catService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "catService.requests", groupId = "cat-service-group", containerFactory = "catRequestKafkaListenerContainerFactory")
    public void handleRequest(CatRequestMessage request) {
        CatResponseMessage response;
        try {
            Map<String, Object> payload = new HashMap<>();
            switch (request.getOperation()) {
                case "GET_RANDOM_CAT" -> {
                    var catOpt = catService.getRandomCat();
                    catOpt.ifPresent(cat -> {
                        payload.put("cat", cat);
                        payload.put("likeCount", catService.getLikeCount(cat));
                        payload.put("dislikeCount", catService.getDislikeCount(cat));
                    });
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, payload);
                }
                case "GET_MY_CATS" -> {
                    Long authorId = ((Number) request.getPayload().get("authorId")).longValue();
                    int page = ((Number) request.getPayload().get("page")).intValue();
                    int size = ((Number) request.getPayload().get("size")).intValue();
                    Page<Cat> catPage = catService.getCatsByAuthor(authorId, page, size);
                    payload.put("cats", catPage.getContent());
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, payload);
                }
                case "ADD_CAT" -> {
                    Long chatId = ((Number) request.getPayload().get("chatId")).longValue();
                    String name = (String) request.getPayload().get("name");
                    Object photoObj = request.getPayload().get("photoData");
                    byte[] photoData;
                    if (photoObj instanceof String str) {
                        photoData = java.util.Base64.getDecoder().decode(str);
                    } else {
                        photoData = (byte[]) photoObj;
                    }
                    catService.addCat(chatId, name, photoData);
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, null);
                }
                case "DELETE_CAT" -> {
                    Long catId = ((Number) request.getPayload().get("catId")).longValue();
                    Long chatId = ((Number) request.getPayload().get("chatId")).longValue();
                    catService.deleteCat(catId, chatId);
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, null);
                }
                case "GET_CAT_DETAILS" -> {
                    Long catId = ((Number) request.getPayload().get("catId")).longValue();
                    var catOpt = catService.getCatById(catId);
                    catOpt.ifPresent(cat -> payload.put("cat", cat));
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, payload);
                }
                case "RATE_CAT" -> {
                    Long catId = ((Number) request.getPayload().get("catId")).longValue();
                    Long userId = ((Number) request.getPayload().get("userId")).longValue();
                    boolean isLike = (Boolean) request.getPayload().get("isLike");
                    catService.rateCat(catId, userId, isLike);
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, null);
                }
                case "FIND_USER_BY_CHAT_ID" -> {
                    Long chatId = ((Number) request.getPayload().get("chatId")).longValue();
                    var userOpt = catService.getUserService().findByChatId(chatId);
                    userOpt.ifPresent(user -> payload.put("user", user));
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, payload);
                }
                case "CREATE_USER" -> {
                    Long chatId = ((Number) request.getPayload().get("chatId")).longValue();
                    String name = (String) request.getPayload().get("name");
                    var user = catService.getUserService().createUser(chatId, name);
                    payload.put("user", user);
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, payload);
                }
                case "IS_USER_EXISTS" -> {
                    Long chatId = ((Number) request.getPayload().get("chatId")).longValue();
                    boolean exists = catService.getUserService().isUserExists(chatId);
                    payload.put("exists", exists);
                    response = new CatResponseMessage(request.getRequestId(), "OK", null, payload);
                }
                default -> response = new CatResponseMessage(request.getRequestId(), "ERROR", "Unknown operation", null);
            }
        } catch (Exception e) {
            response = new CatResponseMessage(request.getRequestId(), "ERROR", e.getMessage(), null);
        }
        kafkaTemplate.send("catService.responses", response);
    }
} 