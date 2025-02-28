package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserSessionService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AwaitNameHandler implements StateHandler {

    @Override
    public void handle(Update update, Long chatId, UserSessionService sessionService, CatsBot bot) {
        String name = update.getMessage().getText();
        sessionService.storeTempData(chatId, "userName", name);

        bot.sendMessage(SendMessage.builder().chatId(chatId).text("Ваше имя сохранено!").build());
        sessionService.setState(chatId, UserState.MAIN_MENU);

        StateHandler nextHandler = bot.getHandlers().get(UserState.MAIN_MENU);
        nextHandler.handle(update, chatId, sessionService, bot);
    }
}
