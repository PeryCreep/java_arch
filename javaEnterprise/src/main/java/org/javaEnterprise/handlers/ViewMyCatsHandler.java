package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.services.UserSessionService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class ViewMyCatsHandler implements StateHandler {
    @Override
    public void handle(Update update, UserSessionService sessionService, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text(MessageBundle.getMessage("button.back"))
                                .callbackData("MAIN_MENU")
                                .build()
                )))
                .build();

        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(keyboard)
                .text(MessageBundle.getMessage("view.my.cats"))
                .build());//todo вызов бизнес логики
    }
}
