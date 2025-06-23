package org.javaEnterprise.handlers;

import org.common.domain.Cat;
import org.common.kafka.payloads.GetRandomCatResponsePayload;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.ErrorHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.javaEnterprise.kafka.CatKafkaService;
import org.common.kafka.dto.CatRequestMessage;
import org.common.kafka.dto.CatResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.TimeUnit;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import org.common.kafka.dto.CatOperationType;
import org.common.kafka.payloads.GetRandomCatPayload;
import org.common.kafka.payloads.RateCatPayload;
import org.common.kafka.payloads.SingleCatResponsePayload;

@Component
public class ViewRandomCatHandler implements StateHandler {

    private final CatKafkaService catKafkaService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ViewRandomCatHandler(CatKafkaService catKafkaService) {
        this.catKafkaService = catKafkaService;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String callbackData = update.getCallbackQuery().getData();
        String[] parts = callbackData.split("_");

        if (update.hasCallbackQuery() && parts[0].equals(ActionPrefixConstants.LIKE.name())) {
            handleRatingCallback(update, bot, true, Long.parseLong(parts[1]));
            return;
        } else if(update.hasCallbackQuery() && parts[0].equals(ActionPrefixConstants.DISLIKE.name())) {
            handleRatingCallback(update, bot, false, Long.parseLong(parts[1]));
            return;
        }

        CatRequestMessage request = new CatRequestMessage(
            CatOperationType.GET_RANDOM_CAT,
            new GetRandomCatPayload(),
            System.currentTimeMillis(),
            chatId
        );
        try {
            CatResponseMessage response = catKafkaService.sendRequest(request).get(5, TimeUnit.SECONDS);
            if ("OK".equals(response.getStatus()) && response.getPayload() instanceof GetRandomCatResponsePayload payload) {
                Cat cat = payload.getCat();
                long likeCount = payload.getLikeCount() != null ? payload.getLikeCount() : 0;
                long dislikeCount = payload.getDislikeCount() != null ? payload.getDislikeCount() : 0;
                sendCatWithButtons(cat, chatId, bot, likeCount, dislikeCount);
            } else {
                bot.sendMessage(SendMessage.builder().chatId(chatId)
                        .text(MessageBundle.getMessage("error.no.cats"))
                        .build());
            }
        } catch (Exception e) {
            bot.sendMessage(SendMessage.builder().chatId(chatId)
                    .text(MessageBundle.getMessage("error.no.cats"))
                    .build());
        }
    }

    private void handleRatingCallback(Update update, ITelegramMessageWorker bot, Boolean isLike, Long catId) {
        Long chatId = bot.getChatId(update);
        try {
            CatRequestMessage request = new CatRequestMessage(
                CatOperationType.RATE_CAT,
                new RateCatPayload(catId, chatId, isLike),
                System.currentTimeMillis(),
                chatId
            );
            CatResponseMessage response = catKafkaService.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!"OK".equals(response.getStatus())) {
                ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("error.cat.rate"));
                return;
            }
            DeleteMessage deleteMsg = DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(update.getCallbackQuery().getMessage().getMessageId())
                    .build();
            bot.deleteMessage(deleteMsg);
            CatRequestMessage randomRequest = new CatRequestMessage(
                CatOperationType.GET_RANDOM_CAT,
                new GetRandomCatPayload(),
                System.currentTimeMillis(),
                chatId
            );
            CatResponseMessage randomResponse = catKafkaService.sendRequest(randomRequest).get(5, java.util.concurrent.TimeUnit.SECONDS);
            if ("OK".equals(randomResponse.getStatus()) && randomResponse.getPayload() instanceof GetRandomCatResponsePayload payload) {
                long likeCount = payload.getLikeCount() != null ? payload.getLikeCount() : 0;
                long dislikeCount = payload.getDislikeCount() != null ? payload.getDislikeCount() : 0;
                sendCatWithButtons(payload.getCat(), chatId, bot, likeCount, dislikeCount);
            } else {
                bot.sendMessage(SendMessage.builder().chatId(chatId)
                        .text(MessageBundle.getMessage("error.no.cats"))
                        .build());
            }
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, e);
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
