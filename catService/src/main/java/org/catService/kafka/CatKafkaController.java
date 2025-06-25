package org.catService.kafka;

import org.common.kafka.dto.*;
import org.catService.services.CatService;
import org.common.domain.Cat;
import org.common.kafka.payloads.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Page;

@Component
public class CatKafkaController {
    private final CatService catService;
    private final KafkaTemplate<String, CatResponseMessage> kafkaTemplate;

    public CatKafkaController(CatService catService, KafkaTemplate<String, CatResponseMessage> kafkaTemplate) {
        this.catService = catService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "catService.requests", groupId = "cat-service-group", containerFactory = "catRequestKafkaListenerContainerFactory")
    public void handleRequest(CatRequestMessage request) {
        CatResponseMessage response;
        try {
            CatOperationType operation = request.getOperation();
            switch (operation) {
                case GET_RANDOM_CAT -> {
                    var payloadOpt = catService.getRandomCatPayload();
                    response = payloadOpt.map(getRandomCatPayload -> new CatResponseMessage(request.getRequestId(), null, getRandomCatPayload))
                            .orElseGet(() -> new CatResponseMessage(request.getRequestId(), "Cat not found", null));
                }
                case GET_MY_CATS -> {
                    GetMyCatsPayload payload = (GetMyCatsPayload) request.getPayload();
                    var userOpt = catService.getUserService().findByChatId(payload.getChatId());
                    if (userOpt.isEmpty()) {
                        response = new CatResponseMessage(request.getRequestId(), "User not found", new ErrorResponsePayload("User not found"));
                    } else {
                        Page<Cat> catPage = catService.getCatsByAuthor(userOpt.get().getId(), payload.getPage(), payload.getSize());
                        response = new CatResponseMessage(request.getRequestId(), null,
                                new CatListResponsePayload(catPage.getContent(), payload.getPage(), payload.getSize()));
                    }
                }
                case CREATE_CAT -> {
                    CreateCatPayload payload = (CreateCatPayload) request.getPayload();
                    catService.addCat(payload.getChatId(), payload.getName(), payload.getPhotoData());
                    response = new CatResponseMessage(request.getRequestId(), null, new SuccessResponsePayload("Cat created"));
                }
                case DELETE_CAT -> {
                    DeleteCatPayload payload = (DeleteCatPayload) request.getPayload();
                    catService.deleteCat(payload.getCatId(), payload.getChatId());
                    response = new CatResponseMessage(request.getRequestId(), null, new SuccessResponsePayload("Cat deleted"));
                }
                case GET_CAT_BY_ID -> {
                    GetCatByIdPayload payload = (GetCatByIdPayload) request.getPayload();
                    var catOpt = catService.getCatById(payload.getCatId());
                    if (catOpt.isPresent()) {
                        response = new CatResponseMessage(request.getRequestId(), null,
                                new SingleCatResponsePayload(catOpt.get()));
                    } else {
                        response = new CatResponseMessage(request.getRequestId(), "Cat not found", null);
                    }
                }
                case RATE_CAT -> {
                    RateCatPayload payload = (RateCatPayload) request.getPayload();
                    catService.rateCat(payload.getCatId(), payload.getUserId(), payload.isLike());
                    response = new CatResponseMessage(request.getRequestId(), null, new SuccessRateResponsePayload());
                }
                case FIND_USER_BY_CHAT_ID -> {
                    FindUserByChatIdPayload payload = (FindUserByChatIdPayload) request.getPayload();
                    var userOpt = catService.getUserService().findByChatId(payload.getChatId());
                    if (userOpt.isPresent()) {
                        response = new CatResponseMessage(request.getRequestId(), null,
                                new UserResponsePayload(userOpt.get()));
                    } else {
                        response = new CatResponseMessage(request.getRequestId(), "User not found", null);
                    }
                }
                case CREATE_USER -> {
                    CreateUserPayload payload = (CreateUserPayload) request.getPayload();
                    var user = catService.getUserService().createUser(payload.getChatId(), payload.getName());
                    response = new CatResponseMessage(request.getRequestId(), null,
                            new UserResponsePayload(user));
                }
                case IS_USER_EXISTS -> {
                    IsUserExistsPayload payload = (IsUserExistsPayload) request.getPayload();
                    boolean exists = catService.getUserService().isUserExists(payload.getChatId());
                    response = new CatResponseMessage(request.getRequestId(), null,
                            new UserExistsResponsePayload(exists));
                }
                default -> response = new CatResponseMessage(request.getRequestId(), "Unknown operation", new ErrorResponsePayload("Unknown operation"));
            }
        } catch (Exception e) {
            response = new CatResponseMessage(request.getRequestId(), e.getMessage(), new ErrorResponsePayload(e.getMessage()));
        }
        if(response.getPayload() instanceof CatListResponsePayload || response.getPayload() instanceof GetRandomCatResponsePayload || response.getPayload() instanceof SuccessRateResponsePayload) {
            kafkaTemplate.send("catService.responsesAsync", response);
        } else {
            kafkaTemplate.send("catService.responses", response);
        }
    }
} 