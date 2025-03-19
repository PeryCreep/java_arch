package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserSessionService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AwaitNameHandler implements StateHandler {

    @Override
    public void handle(Update update, UserSessionService sessionService, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String name = update.getMessage().getText();
        sessionService.storeTempData(chatId, "userName", name);
        sessionService.markUserAsInitialized(chatId);

        bot.sendMessage(SendMessage.builder().chatId(chatId).text(MessageBundle.getMessage("view.name.saved")).build());
        sessionService.setState(chatId, UserState.MAIN_MENU);

        StateHandler nextHandler = bot.getHandlers().get(UserState.MAIN_MENU);
        nextHandler.handle(update, sessionService, bot);
    }
}
