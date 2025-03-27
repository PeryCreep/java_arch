package org.javaEnterprise.util;

import org.javaEnterprise.controllers.CatsBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;


public class ErrorHandler {


    public static void handleError(Long chatId, CatsBot bot, Exception exception) {
        String errorMessage = formatErrorMessage(exception);
        sendErrorMessage(chatId, bot, errorMessage);
        logError(exception);
    }

    public static void handleError(Long chatId, CatsBot bot, String errorMessage) {
        sendErrorMessage(chatId, bot, errorMessage);
    }

    private static String formatErrorMessage(Exception exception) {
        if (exception instanceof SecurityException) {
            return MessageBundle.getMessage("error.access.denied");
        } else if (exception instanceof IllegalStateException) {
            return exception.getMessage();
        } else {
            return MessageBundle.getMessage("error.general");
        }
    }


    private static void sendErrorMessage(Long chatId, CatsBot bot, String errorMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(errorMessage);
        bot.sendMessage(message);
    }

    private static void logError(Exception exception) {
        System.err.println("Error occurred: " + exception.getMessage());
        exception.printStackTrace();
    }
} 