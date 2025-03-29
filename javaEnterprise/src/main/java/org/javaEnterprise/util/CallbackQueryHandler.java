package org.javaEnterprise.util;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.HandlerProvider;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.enums.CallbackData;
import org.telegram.telegrambots.meta.api.objects.Update;

public class CallbackQueryHandler {

    public static boolean processCallbackQuery(Update update, CatsBot bot, HandlerProvider handlerProvider) {
        if (!update.hasCallbackQuery()) {
            return false;
        }

        String callbackData = update.getCallbackQuery().getData();
        Long chatId = bot.getChatId(update);
        
        if (callbackData.startsWith("LIKE_") || callbackData.startsWith("DISLIKE_")) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_RANDOM_CAT);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.startsWith("VIEW_CAT_")) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_CAT_DETAILS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.equals(CallbackData.MYCATS_BACK.name())) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_MY_CATS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.startsWith("MYCATS_PAGE_")) {
            int page = Integer.parseInt(callbackData.split("_")[2]);
            bot.getUserDataFacade().storePage(chatId, page);
            StateHandler handler = handlerProvider.get(UserState.VIEW_MY_CATS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.startsWith("DELETE_CAT_")) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_MY_CATS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        try {
            UserState newState = UserState.valueOf(callbackData);
            bot.getUserDataFacade().setState(chatId, newState);
            StateHandler handler = handlerProvider.get(newState);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
} 