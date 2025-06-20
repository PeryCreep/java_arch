package org.javaEnterprise.controllers;

import org.javaEnterprise.handlers.HandlerProvider;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.util.CallbackQueryHandler;
import org.javaEnterprise.util.CommandHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class CatsBot extends TelegramLongPollingBot implements ITelegramMessageWorker {

    private final HandlerProvider handlerProvider;
    private final UserDataFacade userDataFacade;

    @Autowired
    public CatsBot(HandlerProvider handlerProvider, UserDataFacade userDataFacade) {
        this.handlerProvider = handlerProvider;
        this.userDataFacade = userDataFacade;
    }


    public UserDataFacade getUserDataFacade() {
        return userDataFacade;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = getChatId(update);
        if (chatId == null) return;

        if (update.hasMessage() && update.getMessage().hasText()) {
            if (CommandHandler.processCommand(update, this, handlerProvider)) {
                return;
            }
        }

        if (update.hasCallbackQuery()) {
            if (CallbackQueryHandler.processCallbackQuery(update, this, handlerProvider)) {
                return;
            }
        }

        UserState currentState = userDataFacade.getCurrentState(chatId);
        StateHandler handler = handlerProvider.get(currentState);

        if (handler != null) {
            handler.handle(update, this, userDataFacade);
        } else {
            sendUnknownStateMessage(chatId);
            userDataFacade.setState(chatId, UserState.MAIN_MENU);
            handlerProvider.get(UserState.MAIN_MENU).handle(update, this, userDataFacade);
        }
    }

    private void sendUnknownStateMessage(Long chatId) {
        sendMessage(SendMessage.builder()
                .chatId(chatId.toString())
                .text(MessageBundle.getMessage("view.unknown.state"))
                .build()
        );
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
            System.err.println("Error sending photo: " + e.getMessage());
        }
    }

    public void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteMessage(DeleteMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error deleting message: " + e.getMessage());
        }
    }

    public void editMessage(EditMessageText editMessage) {
        try {
            execute(editMessage);
        } catch (TelegramApiException e) {
            System.err.println("Error editing message: " + e.getMessage());
        }
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
            System.err.println("Error downloading photo: " + e.getMessage());
            return null;
        }
    }

    private String getFileUrl(String fileId) {
        try {
            return execute(org.telegram.telegrambots.meta.api.methods.GetFile.builder()
                    .fileId(fileId)
                    .build()).getFileUrl(getBotToken());
        } catch (TelegramApiException e) {
            System.err.println("Error getting file URL: " + e.getMessage());
            return null;
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
