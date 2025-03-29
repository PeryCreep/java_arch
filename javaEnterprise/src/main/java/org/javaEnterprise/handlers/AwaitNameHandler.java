package org.javaEnterprise.handlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.UserService;
import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class AwaitNameHandler implements StateHandler {

    private final UserService userService;

    private final HandlerProvider handlerProvider;

    public AwaitNameHandler(UserService userService, HandlerProvider handlerProvider) {
        this.userService = userService;
        this.handlerProvider = handlerProvider;
    }

    @Override
    public void handle(Update update, CatsBot bot, UserDataFacade userDataFacade) {
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
        userService.saveUserName(chatId, name);

        StateHandler nextHandler = nextHandler();
        nextHandler.handle(update, bot, userDataFacade);
    }

    @Override
    public StateHandler nextHandler() {
        return handlerProvider.get(UserState.MAIN_MENU);
    }
}
