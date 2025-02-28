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
public class ViewMyCatsHandler implements StateHandler {
    @Override
    public void handle(Update update, Long chatId, UserSessionService sessionService, CatsBot bot) {
        List<InlineKeyboardButton> row = new ArrayList<>(List.of(
                InlineKeyboardButton.builder().text("Назад").callbackData("MAIN_MENU").build()
        ));


        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .build();
        bot.sendMessage(SendMessage.builder().chatId(chatId).replyMarkup(keyboard).text("Просмотр моих котиков").build());
    }
}
