package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartStateHandler implements StateHandler {

    @Override
    public void handle(Update update, Long chatId, UserSessionService sessionService, CatsBot bot) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Привет! Как тебя зовут?");
        sessionService.setState(chatId, UserState.AWAIT_NAME);
        bot.sendMessage(message);
    }
}
