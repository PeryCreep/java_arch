package org.javaEnterprise.handlers.stateHanlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.services.enums.UserTempDataKey;
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
public class AddCatConfirmStateHandler implements StateHandler {
    private final HandlerProvider handlerProvider;

    public AddCatConfirmStateHandler(HandlerProvider handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (update.hasCallbackQuery() && CallbackData.RESTART_ADD_CAT.name().equals(update.getCallbackQuery().getData())) {
            userDataFacade.setState(chatId, UserState.ADD_CAT_IMAGE);
            StateHandler handler = handlerProvider.get(UserState.ADD_CAT_IMAGE);
            handler.handle(update, bot, userDataFacade);
            return;
        }
        if (update.hasCallbackQuery() && CallbackData.CONFIRM_ADD_CAT.name().equals(update.getCallbackQuery().getData())) {
            userDataFacade.setState(chatId, UserState.ADD_CAT_SAVE);
            StateHandler handler = handlerProvider.get(UserState.ADD_CAT_SAVE);
            handler.handle(update, bot, userDataFacade);
            return;
        }

        String catName = userDataFacade.getTextData(chatId, UserTempDataKey.CAT_NAME.name());
        byte[] photoData = userDataFacade.getFormData(chatId, UserTempDataKey.CAT_PHOTO_DATA.name(), byte[].class);

        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId.toString())
                .photo(new InputFile(new ByteArrayInputStream(photoData), "cat_preview.jpg"))
                .caption(catName + "\n" + MessageBundle.getMessage("add.cat.confirm"))
                .replyMarkup(createConfirmKeyboard())
                .build();
        bot.sendPhoto(sendPhoto);
    }

    private InlineKeyboardMarkup createConfirmKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.confirm"))
                                        .callbackData(CallbackData.CONFIRM_ADD_CAT.name())
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.restart"))
                                        .callbackData(CallbackData.RESTART_ADD_CAT.name())
                                        .build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.back"))
                                        .callbackData(CallbackData.MAIN_MENU.name())
                                        .build()
                        )
                ))
                .build();
    }
} 