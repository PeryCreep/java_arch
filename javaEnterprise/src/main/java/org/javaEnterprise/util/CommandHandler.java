package org.javaEnterprise.util;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.stateHanlers.HandlerProvider;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;
import org.telegram.telegrambots.meta.api.objects.Update;


public class CommandHandler {

    public static boolean processCommand(Update update, CatsBot bot, HandlerProvider handlerProvider) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return false;
        }

        String text = update.getMessage().getText();
        Long chatId = bot.getChatId(update);
        UserDataFacade userDataFacade = bot.getUserDataFacade();

        if (!isCommand(text)) {
            return false;
        }

        String command = text.startsWith("/") ? text.substring(1) : text;
        command = command.toLowerCase();

        UserState newState = null;

        switch (command) {
            case "start":
                newState = UserState.START;
                break;
            case "menu":
            case "main":
                newState = UserState.MAIN_MENU;
                break;
            case "random":
            case "randomcat":
                newState = UserState.VIEW_RANDOM_CAT;
                break;
            case "mycats":
            case "cats":
                newState = UserState.VIEW_MY_CATS;
                break;
            case "addcat":
            case "add":
                newState = UserState.ADD_CAT_NAME;
                break;
            default:
                return false;
        }

        if (newState != null) {
            userDataFacade.setState(chatId, newState);
            StateHandler handler = handlerProvider.get(newState);
            handler.handle(update, bot, userDataFacade);
            return true;
        }

        return false;
    }

    private static boolean isCommand(String text) {
        return text != null && text.startsWith("/");
    }
} 