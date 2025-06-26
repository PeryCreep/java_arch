package org.javaEnterprise.kafka;

import org.common.kafka.dto.CatRequestMessage;
import org.common.kafka.dto.CatResponseMessage;
import org.javaEnterprise.handlers.kafkaHandlers.RateCatResponseHandlerKafka;
import org.javaEnterprise.handlers.kafkaHandlers.ViewMyCatsResponseHandlerKafka;
import org.javaEnterprise.handlers.kafkaHandlers.ViewRandomCatResponseHandlerKafka;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.handlers.states.IUniversalKafkaResponseHandler;
import org.javaEnterprise.services.UserDataFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CatKafkaService {
    private final KafkaTemplate<String, CatRequestMessage> kafkaTemplate;
    private final ConcurrentMap<Long, CompletableFuture<CatResponseMessage>> responseFutures = new ConcurrentHashMap<>();

    @Autowired
    private ITelegramMessageWorker catsBot;
    @Autowired
    private UserDataFacade userDataFacade;

    public CatKafkaService(KafkaTemplate<String, CatRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<CatResponseMessage> sendRequest(CatRequestMessage request) {
        CompletableFuture<CatResponseMessage> future = new CompletableFuture<>();
        responseFutures.put(request.getRequestId(), future);
        kafkaTemplate.send("catService.requests", request);
        return future;
    }

    @KafkaListener(topics = "catService.responses", groupId = "java-enterprise-group", containerFactory = "catResponseKafkaListenerContainerFactory")
    public void handleResponse(CatResponseMessage response) {
        CompletableFuture<CatResponseMessage> future = responseFutures.remove(response.getRequestId());
        if (future != null) {
            future.complete(response);
        }
    }

    @KafkaListener(topics = "catService.responsesAsync", groupId = "java-enterprise-universal-group", containerFactory = "catResponseKafkaListenerContainerFactory")
    public void handleUniversalResponse(CatResponseMessage response) {
        IUniversalKafkaResponseHandler handler = null;//todo сделать так же как с обработчиком состояний от бота
        if (response.getPayload() instanceof org.common.kafka.payloads.CatListResponsePayload) {
            handler = new ViewMyCatsResponseHandlerKafka();
        } else if (response.getPayload() instanceof org.common.kafka.payloads.GetRandomCatResponsePayload) {
            handler = new ViewRandomCatResponseHandlerKafka();
        } else if (response.getPayload() instanceof org.common.kafka.payloads.SuccessRateResponsePayload) {
            handler = new RateCatResponseHandlerKafka(this);
        }
        if (handler != null) {
            handler.handleResponse(response, catsBot, userDataFacade);
        } else {
            catsBot.sendMessage(org.telegram.telegrambots.meta.api.methods.send.SendMessage.builder()
                    .chatId(response.getRequestId())
                    .text("Неизвестный тип ответа")
                    .build());
        }
    }

} 