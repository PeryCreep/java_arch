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
import org.javaEnterprise.kafka.CatKafkaService;
import org.javaEnterprise.kafka.dto.CatRequestMessage;
import org.javaEnterprise.kafka.dto.CatResponseMessage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AddCatSaveStateHandler implements StateHandler {
    private final CatKafkaService catKafkaService;

    public AddCatSaveStateHandler(CatKafkaService catKafkaService) {
        this.catKafkaService = catKafkaService;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String catName = userDataFacade.getTextData(chatId, UserTempDataKey.CAT_NAME.name());
        byte[] photoData = userDataFacade.getFormData(chatId, UserTempDataKey.CAT_PHOTO_DATA.name(), byte[].class);

        try {
            CatRequestMessage request = new CatRequestMessage(
                "ADD_CAT",
                Map.of("chatId", chatId, "name", catName, "photoData", photoData),
                System.currentTimeMillis(),
                chatId
            );
            CatResponseMessage response = catKafkaService.sendRequest(request).get(5, TimeUnit.SECONDS);
            if ("OK".equals(response.getStatus())) {
                userDataFacade.clearFormData(chatId, UserTempDataKey.CAT_PHOTO_DATA.name());
                userDataFacade.clearFormData(chatId, UserTempDataKey.CAT_NAME.name());
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
                        .text(MessageBundle.getMessage("view.add.cat.success"))
                        .build());
            } else {
                bot.sendMessage(SendMessage.builder()
                        .chatId(chatId)
                        .text(MessageBundle.getMessage("view.add.cat.error"))
                        .build());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageBundle.getMessage("view.add.cat.error"))
                    .build());
        }
        userDataFacade.setState(chatId, UserState.MAIN_MENU);
    }
} 