package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.services.UserSessionService;
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
    public void handle(Update update, Long chatId, UserSessionService sessionService, CatsBot bot) {

        List<InlineKeyboardButton> row = new ArrayList<>(List.of(
                InlineKeyboardButton.builder().text("Мои котики").callbackData("VIEW_MY_CATS").build(),
                InlineKeyboardButton.builder().text("Смотреть котиков").callbackData("VIEW_RANDOM_CAT").build(),
                InlineKeyboardButton.builder().text("Добавить котика").callbackData("ADD_CAT_IMAGE").build()
        ));


        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();

        SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("Это бот котиков, вот доступные действия: ")
                        .replyMarkup(keyboard)
                        .build();
        bot.sendMessage(message);
    }

}
