package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.ByteArrayInputStream;
import java.util.List;

@Component
public class CatDetailsHandler implements StateHandler {
    private final CatService catService;

    public CatDetailsHandler(CatService catService) {
        this.catService = catService;
    }

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String callbackData = update.getCallbackQuery().getData();

        if (!callbackData.startsWith("VIEW_CAT_") || callbackData.split("_").length != 3) {
            bot.sendMessage(SendMessage.builder().chatId(chatId).text("Некорректный запрос").build());
            return;
        }

        try {
            Long catId = Long.parseLong(callbackData.split("_")[2]);
            catService.getCatById(catId).ifPresentOrElse(
                    cat -> sendCatDetails(cat, chatId, bot),
                    () -> sendError(chatId, bot)
            );
        } catch (NumberFormatException e) {
            bot.sendMessage(SendMessage.builder().chatId(chatId).text("Ошибка формата ID котика").build());
        }
    }

    private void sendCatDetails(Cat cat, Long chatId, CatsBot bot) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId.toString())
                    .photo(new InputFile(
                            new ByteArrayInputStream(cat.getPhotoData()),
                            "cat_" + cat.getId() + ".jpg"
                    ))
                    .caption(cat.getName())
                    .replyMarkup(createDetailsKeyboard(cat.getId()))
                    .build();

            bot.execute(sendPhoto);
        } catch (Exception e) {
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageBundle.getMessage("error.cat.load"))
                    .build());
        }
    }

    private InlineKeyboardMarkup createDetailsKeyboard(Long catId) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.delete"))
                                        .callbackData("DELETE_CAT_" + catId)
                                        .build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.back"))
                                        .callbackData("MYCATS_BACK")
                                        .build()
                        )
                ))
                .build();
    }

    private void sendError(Long chatId, CatsBot bot) {
        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("error.cat.not_found"))
                .build());
    }
}
