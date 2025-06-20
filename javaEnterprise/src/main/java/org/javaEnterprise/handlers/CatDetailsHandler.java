package org.javaEnterprise.handlers;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.ErrorHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
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
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String callbackData = update.getCallbackQuery().getData();

        if (!callbackData.startsWith(ActionPrefixConstants.VIEW_CAT.name() + "_") || callbackData.split("_").length != 3) {
            ErrorHandler.handleError(chatId, bot, "Некорректный запрос");
            return;
        }

        try {
            Long catId = Long.parseLong(callbackData.split("_")[2]);
            catService.getCatById(catId).ifPresentOrElse(
                    cat -> sendCatDetails(cat, chatId, bot),
                    () -> ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("error.cat.not_found"))
            );
        } catch (NumberFormatException e) {
            ErrorHandler.handleError(chatId, bot, "Ошибка формата ID котика");
        }
    }

    private void sendCatDetails(Cat cat, Long chatId, ITelegramMessageWorker bot) {
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

            bot.sendPhoto(sendPhoto);
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("error.cat.load"));
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
                                        .callbackData(CallbackData.MYCATS_BACK.name())
                                        .build()
                        )
                ))
                .build();
    }
}
