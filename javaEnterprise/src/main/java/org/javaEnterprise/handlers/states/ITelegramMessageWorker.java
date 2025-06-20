package org.javaEnterprise.handlers.states;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ITelegramMessageWorker {
    Long getChatId(Update update);
    void sendPhoto(SendPhoto sendPhoto);
    void sendMessage(SendMessage sendMessage);
    void deleteMessage(DeleteMessage deleteMessage);

    void editMessage(EditMessageText editMessage);

    byte[] getPhotoData(Update update);
}
