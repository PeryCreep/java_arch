package org.javaEnterprise;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class EchoBot extends TelegramLongPollingBot {


    @Override
    public String getBotUsername() {
        return "JavaEnterprise";
    }

    @Override
    public void onUpdateReceived(Update update) {
        String message_text = update.getMessage().getText();
        long chat_id = update.getMessage().getChatId();
        SendMessage message = new SendMessage();
        message.setChatId(chat_id);
        message.setText(message_text);
        try {
            this.sendApiMethod(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return System.getenv("TOKEN");
    }
}
