package org.javaEnterprise.handlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;

import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.javaEnterprise.kafka.CatKafkaService;
import org.javaEnterprise.kafka.dto.CatRequestMessage;
import org.javaEnterprise.kafka.dto.CatResponseMessage;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class StartStateHandler implements StateHandler {

    private final CatKafkaService catKafkaService;

    private final HandlerProvider handlerProvider;

    public StartStateHandler(CatKafkaService catKafkaService, HandlerProvider handlerProvider) {
        this.catKafkaService = catKafkaService;
        this.handlerProvider = handlerProvider;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        try {
            CatRequestMessage req = new CatRequestMessage("IS_USER_EXISTS", Map.of("chatId", chatId), System.currentTimeMillis(), chatId);
            CatResponseMessage resp = catKafkaService.sendRequest(req).get(5, TimeUnit.SECONDS);
            boolean exists = resp.getPayload() != null && Boolean.TRUE.equals(resp.getPayload().get("exists"));
            if (exists) {
                userDataFacade.setState(chatId, UserState.MAIN_MENU);
                StateHandler mainMenuHandler = nextHandler();
                mainMenuHandler.handle(update, bot, userDataFacade);
                return;
            }
        } catch (Exception e) {
            bot.sendMessage(new SendMessage(chatId.toString(), "Ошибка при проверке пользователя"));
            return;
        }

        SendMessage welcomeMessage = new SendMessage();
        welcomeMessage.setChatId(chatId.toString());
        welcomeMessage.setText(MessageBundle.getMessage("view.hi"));
        bot.sendMessage(welcomeMessage);
        
        userDataFacade.setState(chatId, UserState.AWAIT_NAME);
    }

    @Override
    public StateHandler nextHandler() {
        return handlerProvider.get(UserState.MAIN_MENU);
    }
}
