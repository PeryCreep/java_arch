package org.javaEnterprise.handlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;

import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.services.enums.UserTempDataKey;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;


@Component
public class AddCatImageStateHandler implements StateHandler {

    private final HandlerProvider handlerProvider;

    public AddCatImageStateHandler(HandlerProvider handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (update.hasMessage() && update.getMessage().hasPhoto()) {
            byte[] photoData = bot.getPhotoData(update);
            if (photoData != null) {
                userDataFacade.storeFormData(chatId, UserTempDataKey.CAT_PHOTO_DATA.name(), photoData);
                userDataFacade.setState(chatId, UserState.ADD_CAT_NAME);

                StateHandler nameHandler = nextHandler();
                if (nameHandler != null) {
                    nameHandler.handle(update, bot, userDataFacade);
                }
                return;
            }
        } else {
            userDataFacade.setState(chatId, UserState.ADD_CAT_IMAGE);
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text(MessageBundle.getMessage("button.back"))
                                .callbackData(CallbackData.MAIN_MENU.name())
                                .build()
                )))
                .build();

        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(keyboard)
                .text(MessageBundle.getMessage("view.add.cat.photo"))
                .build());
    }

    @Override
    public StateHandler nextHandler() {
        return handlerProvider.get(UserState.ADD_CAT_NAME);
    };
}
