package org.javaEnterprise.handlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.services.enums.UserTempDataKey;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class AddCatNameStateHandler implements StateHandler {

    private final HandlerProvider handlerProvider;

    public AddCatNameStateHandler(HandlerProvider handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    public void handle(Update update, CatsBot bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            if (CallbackData.CANCEL_ADD_CAT.name().equals(callbackData)) {
                userDataFacade.setState(chatId, UserState.MAIN_MENU);
                StateHandler handler = handlerProvider.get(UserState.MAIN_MENU);
                handler.handle(update, bot, userDataFacade);
                return;
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String catName = update.getMessage().getText();
            userDataFacade.storeTextData(chatId, UserTempDataKey.CAT_NAME.name(), catName);

            userDataFacade.setState(chatId, UserState.ADD_CAT_SAVE);
            StateHandler handler = nextHandler();
            handler.handle(update, bot, userDataFacade);
            return;
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text(MessageBundle.getMessage("button.back"))
                                .callbackData(CallbackData.CANCEL_ADD_CAT.name())
                                .build()
                )))
                .build();

        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("add.cat.enter.name"))
                .replyMarkup(keyboard)
                .build());
    }

    @Override
    public StateHandler nextHandler() {
        return handlerProvider.get(UserState.ADD_CAT_SAVE);
    }
}