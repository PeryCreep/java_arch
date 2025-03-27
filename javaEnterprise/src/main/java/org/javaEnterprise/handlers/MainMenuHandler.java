package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class MainMenuHandler implements StateHandler {

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        List<InlineKeyboardButton> row = new ArrayList<>(List.of(
                InlineKeyboardButton.builder().text(MessageBundle.getMessage("button.my.cat")).callbackData("VIEW_MY_CATS").build(),
                InlineKeyboardButton.builder().text(MessageBundle.getMessage("button.random.cat")).callbackData("VIEW_RANDOM_CAT").build(),
                InlineKeyboardButton.builder().text(MessageBundle.getMessage("button.add.cat")).callbackData("ADD_CAT_IMAGE").build()
        ));

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("view.main"))
                .replyMarkup(keyboard)
                .build();
        bot.sendMessage(message);
    }

}
