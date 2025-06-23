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
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Component
public class AwaitNameHandler implements StateHandler {


    private final HandlerProvider handlerProvider;

    private final CatKafkaService catKafkaService;

    public AwaitNameHandler( HandlerProvider handlerProvider, CatKafkaService catKafkaService) {
        this.handlerProvider = handlerProvider;
        this.catKafkaService = catKafkaService;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageBundle.getMessage("await.name.retry"))
                    .build());
            return;
        }

        String name = update.getMessage().getText();
        try {
            CatRequestMessage req = new CatRequestMessage("CREATE_USER", Map.of("chatId", chatId, "name", name), System.currentTimeMillis(), chatId);
            catKafkaService.sendRequest(req).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text("Ошибка при сохранении пользователя")
                    .build());
            return;
        }

        StateHandler nextHandler = nextHandler();
        nextHandler.handle(update, bot, userDataFacade);
    }

    @Override
    public StateHandler nextHandler() {
        return handlerProvider.get(UserState.MAIN_MENU);
    }
}
