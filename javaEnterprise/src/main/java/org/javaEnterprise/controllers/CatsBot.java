package org.javaEnterprise.controllers;

import org.javaEnterprise.handlers.MainMenuHandler;
import org.javaEnterprise.handlers.StartStateHandler;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.EnumMap;
import java.util.Map;

@Component
public class CatsBot extends TelegramLongPollingBot {

    @Autowired private UserSessionService sessionService;
    private Map<UserState, StateHandler> handlers;

    public CatsBot(UserSessionService sessionService, Map<UserState, StateHandler> handlers) {
        this.sessionService = sessionService;
        this.handlers = handlers;
    }

    public Map<UserState, StateHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getChatId(update);
        if (chatId == null) return;
        if (update.hasCallbackQuery()) {
            sessionService.setState(chatId, UserState.valueOf(update.getCallbackQuery().getData()));
        }

        UserState currentState = sessionService.getCurrentState(chatId);
        StateHandler handler = handlers.get(currentState);

        if (handler != null) {
            handler.handle(update, chatId, sessionService, this);
        } else {
            handleUnknownState(chatId);
        }

    }

    private Long getChatId(Update update) {
        if (update.hasMessage()) return update.getMessage().getChatId();
        if (update.hasCallbackQuery()) return update.getCallbackQuery().getMessage().getChatId();
        return null;
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleError(Long chatId, Exception e) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Произошла ошибка: " + e.getMessage());
        sendMessage(message);
    }

    private void handleUnknownState(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Неизвестное состояние, возврат в главное меню");
        sessionService.setState(chatId, UserState.MAIN_MENU);
        sendMessage(message);
    }

    @Override
    public String getBotUsername() {
        return "JavaEnterprise";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TOKEN");
    }
}
