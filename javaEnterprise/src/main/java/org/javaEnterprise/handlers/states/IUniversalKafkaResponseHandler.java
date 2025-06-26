package org.javaEnterprise.handlers.states;

import org.common.kafka.dto.CatResponseMessage;
import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.services.UserDataFacade;

public interface IUniversalKafkaResponseHandler {
    void handleResponse(CatResponseMessage response, ITelegramMessageWorker bot, UserDataFacade userDataFacade);
} 