package org.javaEnterprise.handlers;

import org.javaEnterprise.domain.Cat;
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
import org.javaEnterprise.kafka.dto.CatRequestMessage;
import org.javaEnterprise.kafka.dto.CatResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class ViewMyCatsHandler implements StateHandler {
    private static final int PAGE_SIZE = 9;
    private final CatKafkaService catKafkaService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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

        try {
            CatRequestMessage userReq = new CatRequestMessage("FIND_USER_BY_CHAT_ID", Map.of("chatId", chatId), System.currentTimeMillis(), chatId);
            CatResponseMessage userResp = catKafkaService.sendRequest(userReq).get(5, TimeUnit.SECONDS);
            org.javaEnterprise.domain.User user = null;
            if (userResp.getPayload() != null && userResp.getPayload().get("user") != null) {
                user = objectMapper.convertValue(userResp.getPayload().get("user"), org.javaEnterprise.domain.User.class);
            }
            if (user == null) {
                bot.sendMessage(SendMessage.builder().chatId(chatId).text("Пользователь не найден").build());
                return;
            }
            int page = getCurrentPage(chatId, userDataFacade);
            CatRequestMessage request = new CatRequestMessage(
                "GET_MY_CATS",
                Map.of("authorId", user.getId(), "page", page, "size", PAGE_SIZE),
                System.currentTimeMillis(),
                chatId
            );
            CatResponseMessage response = catKafkaService.sendRequest(request).get(5, TimeUnit.SECONDS);
            if ("OK".equals(response.getStatus()) && response.getPayload() != null && response.getPayload().get("cats") != null) {
                java.util.List<org.javaEnterprise.domain.Cat> cats = objectMapper.convertValue(response.getPayload().get("cats"), new TypeReference<java.util.List<org.javaEnterprise.domain.Cat>>() {});
                if (cats.isEmpty()) {
                    sendEmptyMessage(chatId, bot);
                } else {
                    sendCatsPage(chatId, cats, page, bot, userDataFacade);
                }
            } else {
                sendEmptyMessage(chatId, bot);
            }
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, e);
        }
    }

    private void handleDeleteCat(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split("_")[2]);
        try {
            org.javaEnterprise.kafka.dto.CatRequestMessage request = new org.javaEnterprise.kafka.dto.CatRequestMessage(
                "DELETE_CAT",
                java.util.Map.of("catId", catId, "chatId", chatId),
                System.currentTimeMillis(),
                chatId
            );
            org.javaEnterprise.kafka.dto.CatResponseMessage response = catKafkaService.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!"OK".equals(response.getStatus())) {
                ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("view.cat.delete.error"));
                return;
            }
            editMessageAfterDelete(update, bot);
            int page = getCurrentPage(chatId, userDataFacade);
            CatRequestMessage listRequest = new CatRequestMessage(
                "GET_MY_CATS",
                java.util.Map.of("authorId", chatId, "page", page, "size", PAGE_SIZE),
                System.currentTimeMillis(),
                chatId
            );
            CatResponseMessage listResponse = catKafkaService.sendRequest(listRequest).get(5, java.util.concurrent.TimeUnit.SECONDS);
            java.util.List<Cat> cats = java.util.Collections.emptyList();
            if ("OK".equals(listResponse.getStatus()) && listResponse.getPayload() != null && listResponse.getPayload().get("cats") != null) {
                cats = objectMapper.convertValue(listResponse.getPayload().get("cats"), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<Cat>>() {});
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