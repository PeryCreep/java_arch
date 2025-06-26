package org.javaEnterprise.handlers.stateHanlers;

import org.common.domain.Cat;
import org.common.kafka.payloads.*;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.ErrorHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.javaEnterprise.kafka.CatKafkaService;
import org.common.kafka.dto.CatRequestMessage;
import org.common.kafka.dto.CatResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.common.kafka.dto.CatOperationType;

@Component
public class ViewMyCatsHandler implements StateHandler {
    private static final int PAGE_SIZE = 9;
    private final CatKafkaService catKafkaService;

    public ViewMyCatsHandler(CatKafkaService catKafkaService) {
        this.catKafkaService = catKafkaService;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("DELETE_CAT_")) {
            handleDeleteCat(update, bot, userDataFacade);
            return;
        }

        int page = getCurrentPage(chatId, userDataFacade);
        CatRequestMessage request = new CatRequestMessage(
            CatOperationType.GET_MY_CATS,
            new GetMyCatsPayload(chatId, page, PAGE_SIZE),
            chatId
        );
        catKafkaService.sendRequest(request);
    }

    private void handleDeleteCat(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split("_")[2]);
        try {
            CatRequestMessage request = new CatRequestMessage(
                CatOperationType.DELETE_CAT,
                new DeleteCatPayload(catId, chatId),
                chatId
            );
           CatResponseMessage response = catKafkaService.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
            if (response.getPayload() instanceof ErrorResponsePayload) {
                ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("view.cat.delete.error"));
                return;
            }
            editMessageAfterDelete(update, bot);
            int page = getCurrentPage(chatId, userDataFacade);
            CatRequestMessage listRequest = new CatRequestMessage(
                CatOperationType.GET_MY_CATS,
                new GetMyCatsPayload(chatId, page, PAGE_SIZE),
                chatId
            );
            CatResponseMessage listResponse = catKafkaService.sendRequest(listRequest).get(5, java.util.concurrent.TimeUnit.SECONDS);
            java.util.List<Cat> cats = java.util.Collections.emptyList();
            if (listResponse.getPayload() instanceof CatListResponsePayload payload && payload.getCats() != null) {
                cats = payload.getCats();
            }
            if (cats.isEmpty() && page > 0) {
                userDataFacade.storePage(chatId, page - 1);
                handle(update, bot, userDataFacade);
            } else {
                if (cats.isEmpty()) {
                    sendEmptyMessage(chatId, bot);
                } else {
                    sendCatsPage(chatId, cats, page, bot, userDataFacade);
                }
            }
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, e);
        }
    }

    private void editMessageAfterDelete(Update update, ITelegramMessageWorker bot) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(MessageBundle.getMessage("view.cat.delete.success"))
                .build();

        bot.editMessage(editMessage);
    }

    private int getCurrentPage(Long chatId, UserDataFacade userDataFacade) {
        Integer page = userDataFacade.getPage(chatId);
        return page != null ? page : 0;
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