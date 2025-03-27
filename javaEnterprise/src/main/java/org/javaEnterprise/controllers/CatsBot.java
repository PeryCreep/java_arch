package org.javaEnterprise.controllers;

import org.javaEnterprise.domain.User;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.services.UserService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CatsBot extends TelegramLongPollingBot {

    private final Map<UserState, StateHandler> handlers;
    private final CatService catService;
    private final UserService userService;
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> userTempData = new ConcurrentHashMap<>();

    public CatsBot(Map<UserState, StateHandler> handlers, CatService catService, UserService userService) {
        this.handlers = handlers;
        this.catService = catService;
        this.userService = userService;
    }

    public Map<UserState, StateHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getChatId(update);
        if (chatId == null) return;

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            if (callbackData.startsWith("LIKE_") || callbackData.startsWith("DISLIKE_")) {
                handleRatingCallback(update, callbackData);
                return;
            }

            if (callbackData.startsWith("VIEW_CAT_")) {
                handlers.get(UserState.VIEW_CAT_DETAILS).handle(update, this);
                return;
            }

            if (callbackData.equals("MYCATS_BACK")) {
                handlers.get(UserState.VIEW_MY_CATS).handle(update, this);
                return;
            }

            if (callbackData.equals("MYCATS_BACK")) {
                handlers.get(UserState.VIEW_MY_CATS).handle(update, this);
                return;
            }

            if (callbackData.startsWith("MYCATS_PAGE_")) {
                int page = Integer.parseInt(callbackData.split("_")[2]);
                storeTempData(chatId, "myCatsPage", page);
                handlers.get(UserState.VIEW_MY_CATS).handle(update, this);
                return;
            }

            if (callbackData.startsWith("DELETE_CAT_")) {
                handleDeleteCat(update);
                return;
            }


            setState(chatId, UserState.valueOf(update.getCallbackQuery().getData()));
        }

        UserState currentState = getCurrentState(chatId);
        StateHandler handler = handlers.get(currentState);

        if (handler != null) {
            handler.handle(update, this);
        } else {
            handleUnknownState(chatId, update);
        }
    }

    public Long getChatId(Update update) {
        if (update.hasMessage()) return update.getMessage().getChatId();
        if (update.hasCallbackQuery()) return update.getCallbackQuery().getMessage().getChatId();
        return null;
    }

    public void sendPhoto(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println("Error sending photo: " + e.getMessage());
        }
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleRatingCallback(Update update, String callbackData) {
        Long chatId = getChatId(update);
        String[] parts = callbackData.split("_");
        Long catId = Long.parseLong(parts[1]);

        try {
            if (parts[0].equals("LIKE")) {
                catService.incrementLikes(catId);
            } else {
                catService.incrementDislikes(catId);
            }

            DeleteMessage deleteMsg = DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build();
            execute(deleteMsg);

            handlers.get(UserState.VIEW_RANDOM_CAT).handle(update, this);

        } catch (Exception e) {
            handleError(chatId, e);
        }
    }

    private void handleError(Long chatId, Exception e) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Произошла ошибка: " + e.getMessage());
        sendMessage(message);
    }

    private void handleUnknownState(Long chatId, Update update) {
        sendMessage(SendMessage.builder()
                .chatId(chatId.toString())
                .text(MessageBundle.getMessage("view.unknown.state"))
                .build()
        );
        setState(chatId, UserState.MAIN_MENU);
        handlers.get(UserState.MAIN_MENU).handle(update, this);
    }

    public byte[] getPhotoData(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasPhoto()) {
            return null;
        }

        PhotoSize photo = update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1);
        String fileUrl = getFileUrl(photo.getFileId());

        if (fileUrl == null) {
            return null;
        }

        try {
            URL url = new URL(fileUrl);
            try (InputStream in = url.openStream()) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            System.out.println("Error downloading photo: " + e.getMessage());
            return null;
        }
    }

    private String getFileUrl(String fileId) {
        try {
            return execute(org.telegram.telegrambots.meta.api.methods.GetFile.builder()
                    .fileId(fileId)
                    .build()).getFileUrl(getBotToken());
        } catch (TelegramApiException e) {
            System.out.println("Error getting file URL: " + e.getMessage());
            return null;
        }
    }

    public UserState getCurrentState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.START);
    }

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public void storeTempData(Long chatId, String key, Object value) {
        userTempData
                .computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                .put(key, value);
    }

    public <T> T getTempData(Long chatId, String key, Class<T> clazz) {
        return Optional.ofNullable(userTempData.get(chatId))
                .map(data -> clazz.cast(data.get(key)))
                .orElse(null);
    }

    public void clearTempData(Long chatId, String key) {
        Map<String, Object> userData = userTempData.get(chatId);
        if (userData != null) {
            userData.remove(key);
        }
    }

    private void handleDeleteCat(Update update) {
        Long chatId = getChatId(update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split("_")[2]);
        User user = userService.findByChatId(chatId).orElseThrow();

        try {
            catService.deleteCat(catId, user.getId());
            editMessageAfterDelete(update);
            handlers.get(UserState.VIEW_MY_CATS).handle(update, this);
        } catch (Exception e) {
            handleError(chatId, e);
        }
    }

    private void editMessageAfterDelete(Update update) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(MessageBundle.getMessage("view.cat.delete.success"))
                .build();

        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error editing message: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "JavaEnterprise";
    }

    @Override
    public String getBotToken() {
        return System.getenv("TOKEN");
    }
}
