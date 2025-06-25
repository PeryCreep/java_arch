package org.javaEnterprise.handlers.kafkaHandlers;

import org.common.domain.Cat;
import org.common.kafka.dto.CatResponseMessage;
import org.common.kafka.payloads.CatListResponsePayload;
import org.common.kafka.payloads.ErrorResponsePayload;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.handlers.states.IUniversalKafkaResponseHandler;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.MessageBundle;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ViewMyCatsResponseHandlerKafka implements IUniversalKafkaResponseHandler {
    @Override
    public void handleResponse(CatResponseMessage response, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = response.getRequestId();
        var payload = response.getPayload();


        if (payload instanceof CatListResponsePayload catList) {
            if (catList.getCats() == null || catList.getCats().isEmpty()) {
                sendEmptyMessage(chatId, bot);
            } else {

                sendCatsPage(chatId, catList.getCats(), catList.getPage(), bot, userDataFacade);
            }
        } else if (payload instanceof ErrorResponsePayload error) {
            sendEmptyMessage(chatId, bot);
        }
    }

    private void sendCatsPage(Long chatId, java.util.List<Cat> cats, int page, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        userDataFacade.storePage(chatId, page);
        String caption = String.format(MessageBundle.getMessage("view.my.cats.page"), page + 1, 1);
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(createKeyboard(cats))
                .build();
        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(caption)
                .replyMarkup(keyboard)
                .build());
    }

    private List<List<InlineKeyboardButton>> createKeyboard(java.util.List<Cat> cats) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        cats.forEach(cat -> {
            keyboard.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(cat.getName())
                            .callbackData("VIEW_CAT_" + cat.getId())
                            .build()
            ));
        });
        keyboard.add(List.of(
                InlineKeyboardButton.builder()
                        .text(MessageBundle.getMessage("button.back"))
                        .callbackData(CallbackData.MAIN_MENU.name())
                        .build()
        ));
        return keyboard;
    }

    private void sendEmptyMessage(Long chatId, ITelegramMessageWorker bot) {
        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("view.my.cats.empty"))
                .build());
    }
} 