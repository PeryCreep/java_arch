package org.javaEnterprise.handlers.stateHanlers;

import org.common.domain.Cat;
import org.common.kafka.dto.CatResponseMessage;
import org.common.kafka.payloads.ErrorResponsePayload;
import org.common.kafka.payloads.GetRandomCatResponsePayload;
import org.javaEnterprise.handlers.ActionPrefixConstants;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.ErrorHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.objects.Update;

import org.javaEnterprise.kafka.CatKafkaService;
import org.common.kafka.dto.CatRequestMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import org.common.kafka.dto.CatOperationType;
import org.common.kafka.payloads.GetRandomCatPayload;
import org.common.kafka.payloads.RateCatPayload;

@Component
public class ViewRandomCatHandler implements StateHandler {

    private final CatKafkaService catKafkaService;

    public ViewRandomCatHandler(CatKafkaService catKafkaService) {
        this.catKafkaService = catKafkaService;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String callbackData = update.getCallbackQuery().getData();
        String[] parts = callbackData.split("_");

        if (update.hasCallbackQuery() && parts[0].equals(ActionPrefixConstants.LIKE.name())) {
            handleRatingCallback(update, bot, true, Long.parseLong(parts[1]));
            return;
        } else if(update.hasCallbackQuery() && parts[0].equals(ActionPrefixConstants.DISLIKE.name())) {
            handleRatingCallback(update, bot, false, Long.parseLong(parts[1]));
            return;
        }

        CatRequestMessage request = new CatRequestMessage(
            CatOperationType.GET_RANDOM_CAT,
            new GetRandomCatPayload(),
            chatId
        );
        catKafkaService.sendRequest(request);
    }

    private void handleRatingCallback(Update update, ITelegramMessageWorker bot, Boolean isLike, Long catId) {
        Long chatId = bot.getChatId(update);
        CatRequestMessage request = new CatRequestMessage(
            CatOperationType.RATE_CAT,
            new RateCatPayload(catId, chatId, isLike),
            chatId
        );
        catKafkaService.sendRequest(request);
    }

}
