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
public class StartStateHandler implements StateHandler {

    @Override
    public void handle(Update update, UserSessionService sessionService, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (sessionService.isUserInitialized(chatId)) {
            sessionService.setState(chatId, UserState.MAIN_MENU);
            StateHandler mainMenuHandler = bot.getHandlers().get(UserState.MAIN_MENU);
            mainMenuHandler.handle(update, sessionService, bot);
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(MessageBundle.getMessage("view.hi"));
        sessionService.setState(chatId, UserState.AWAIT_NAME);
        bot.sendMessage(message);
    }
}
