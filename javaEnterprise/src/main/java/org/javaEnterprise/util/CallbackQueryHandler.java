package org.javaEnterprise.util;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.ActionPrefixConstants;
import org.javaEnterprise.handlers.stateHanlers.HandlerProvider;
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
        
        if (callbackData.startsWith(ActionPrefixConstants.LIKE.name() + "_") || callbackData.startsWith(ActionPrefixConstants.DISLIKE.name() + "_")) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_RANDOM_CAT);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.startsWith(ActionPrefixConstants.VIEW_CAT.name() + "_")) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_CAT_DETAILS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.equals(CallbackData.MYCATS_BACK.name())) {
            StateHandler handler = handlerProvider.get(UserState.VIEW_MY_CATS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.startsWith(ActionPrefixConstants.MYCATS_PAGE.name() + "_")) {
            int page = Integer.parseInt(callbackData.split("_")[2]);
            bot.getUserDataFacade().storePage(chatId, page);
            StateHandler handler = handlerProvider.get(UserState.VIEW_MY_CATS);
            handler.handle(update, bot, bot.getUserDataFacade());
            return true;
        }

        if (callbackData.startsWith(ActionPrefixConstants.DELETE_CAT.name() + "_")) {
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