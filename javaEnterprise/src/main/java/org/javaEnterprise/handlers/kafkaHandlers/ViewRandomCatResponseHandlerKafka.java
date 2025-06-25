package org.javaEnterprise.handlers.kafkaHandlers;

import org.common.domain.Cat;
import org.common.kafka.dto.CatResponseMessage;
import org.common.kafka.payloads.GetRandomCatResponsePayload;
import org.common.kafka.payloads.ErrorResponsePayload;
import org.javaEnterprise.handlers.ActionPrefixConstants;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;

import org.javaEnterprise.handlers.states.IUniversalKafkaResponseHandler;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.MessageBundle;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class ViewRandomCatResponseHandlerKafka implements IUniversalKafkaResponseHandler {
    @Override
    public void handleResponse(CatResponseMessage response, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = response.getRequestId();
        var payload = response.getPayload();
        if (payload instanceof GetRandomCatResponsePayload randomCatPayload) {
            long likeCount = randomCatPayload.getLikeCount() != null ? randomCatPayload.getLikeCount() : 0;
            long dislikeCount = randomCatPayload.getDislikeCount() != null ? randomCatPayload.getDislikeCount() : 0;
            sendCatWithButtons(randomCatPayload.getCat(), chatId, bot, likeCount, dislikeCount);

        } else if (payload instanceof ErrorResponsePayload error) {
        bot.sendMessage(SendMessage.builder().chatId(chatId)
                .text(MessageBundle.getMessage("error.no.cats"))
                .build());
        }
    }

    private void sendCatWithButtons(Cat cat, Long chatId, ITelegramMessageWorker bot, long likeCount, long dislikeCount) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId.toString())
                    .photo(new InputFile(new ByteArrayInputStream(cat.getPhotoData()), "cat.jpg"))
                    .caption(String.format(
                            "%s\n%s: %s",
                            cat.getName(),
                            MessageBundle.getMessage("cat.author"),
                            cat.getAuthor().getName()))
                    .replyMarkup(createRatingKeyboard(cat, likeCount, dislikeCount))
                    .build();

            bot.sendPhoto(sendPhoto);

        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageBundle.getMessage("error.cat.send"))
                    .build());
        }
    }

    private InlineKeyboardMarkup createRatingKeyboard(Cat cat, long likeCount, long dislikeCount) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                createRatingButton(ActionPrefixConstants.LIKE.name(), MessageBundle.getMessage("view.random.cat.like"), likeCount, cat.getId()),
                                createRatingButton(ActionPrefixConstants.DISLIKE.name(), MessageBundle.getMessage("view.random.cat.dislike"), dislikeCount, cat.getId())
                        ),
                        List.of(createBackButton())
                ))
                .build();
    }

    private InlineKeyboardButton createRatingButton(String type, String emoji, long count, Long catId) {
        return InlineKeyboardButton.builder()
                .text("%s (%d)".formatted(emoji, count))
                .callbackData("%s_%d".formatted(type, catId))
                .build();
    }

    private InlineKeyboardButton createBackButton() {
        return InlineKeyboardButton.builder()
                .text(MessageBundle.getMessage("button.back"))
                .callbackData(CallbackData.MAIN_MENU.name())
                .build();
    }
} 