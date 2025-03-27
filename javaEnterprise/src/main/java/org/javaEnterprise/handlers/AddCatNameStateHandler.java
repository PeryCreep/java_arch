package org.javaEnterprise.handlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class AddCatNameStateHandler implements StateHandler {

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            if ("CANCEL_ADD_CAT".equals(callbackData)) {
                bot.setState(chatId, UserState.MAIN_MENU);
                StateHandler handler = bot.getHandlers().get(UserState.MAIN_MENU);
                handler.handle(update, bot);
                return;
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String catName = update.getMessage().getText();
            bot.storeTempData(chatId, "cat_name", catName);

            bot.setState(chatId, UserState.ADD_CAT_SAVE);
            StateHandler handler = bot.getHandlers().get(UserState.ADD_CAT_SAVE);
            handler.handle(update, bot);
            return;
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text(MessageBundle.getMessage("button.back"))
                                .callbackData("CANCEL_ADD_CAT")
                                .build()
                )))
                .build();

        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("add.cat.enter.name"))
                .replyMarkup(keyboard)
                .build());
    }
} 