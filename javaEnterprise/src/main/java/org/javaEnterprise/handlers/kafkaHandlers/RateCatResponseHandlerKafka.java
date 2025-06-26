package org.javaEnterprise.handlers.kafkaHandlers;

import org.common.kafka.dto.CatOperationType;
import org.common.kafka.dto.CatRequestMessage;
import org.common.kafka.dto.CatResponseMessage;
import org.common.kafka.payloads.GetRandomCatPayload;
import org.common.kafka.payloads.SuccessRateResponsePayload;
import org.common.kafka.payloads.SuccessResponsePayload;
import org.common.kafka.payloads.ErrorResponsePayload;
import org.javaEnterprise.handlers.states.IUniversalKafkaResponseHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.kafka.CatKafkaService;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;


public class RateCatResponseHandlerKafka implements IUniversalKafkaResponseHandler {
    private final CatKafkaService catKafkaService;

    public RateCatResponseHandlerKafka(CatKafkaService catKafkaService) {
        this.catKafkaService = catKafkaService;
    }
    @Override
    public void handleResponse(CatResponseMessage response, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = response.getRequestId();
        var payload = response.getPayload();
        if (payload instanceof SuccessRateResponsePayload success) {
            CatRequestMessage randomRequest = new CatRequestMessage(
                    CatOperationType.GET_RANDOM_CAT,
                    new GetRandomCatPayload(),
                    chatId
            );
            catKafkaService.sendRequest(randomRequest);
        } else if (payload instanceof ErrorResponsePayload error) {
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(error.getError() != null ? error.getError() : MessageBundle.getMessage("error.cat.rate"))
                    .build());
        }
    }
} 