package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class AddCatSaveStateHandler implements StateHandler {
    private final CatService catService;

    public AddCatSaveStateHandler(CatService catService) {
        this.catService = catService;
    }

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String catName = bot.getTempData(chatId, "cat_name", String.class);
        byte[] photoData = bot.getTempData(chatId, "cat_photo_data", byte[].class);

        try {
            Cat cat = catService.addCat(chatId, catName, photoData);
            bot.clearTempData(chatId, "cat_photo_data");
            bot.clearTempData(chatId, "cat_name");

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
                    .text(MessageBundle.getMessage("view.add.cat.success"))
                    .build());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageBundle.getMessage("view.add.cat.error"))
                    .build());
        }

        bot.setState(chatId, UserState.MAIN_MENU);
    }
} 