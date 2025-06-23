package org.javaEnterprise.handlers;

import org.common.domain.Cat;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.ErrorHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.javaEnterprise.kafka.CatKafkaService;
import org.common.kafka.dto.CatRequestMessage;
import org.common.kafka.dto.CatResponseMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;

import java.io.ByteArrayInputStream;
import java.util.List;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.common.kafka.dto.CatOperationType;
import org.common.kafka.payloads.GetCatByIdPayload;
import org.common.kafka.payloads.SingleCatResponsePayload;

@Component
public class CatDetailsHandler implements StateHandler {
    private final CatKafkaService catKafkaService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CatDetailsHandler(CatKafkaService catKafkaService) {
        this.catKafkaService = catKafkaService;
    }

    @Override
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        String callbackData = update.getCallbackQuery().getData();

        if (!callbackData.startsWith(ActionPrefixConstants.VIEW_CAT.name() + "_") || callbackData.split("_").length != 3) {
            ErrorHandler.handleError(chatId, bot, "Некорректный запрос");
            return;
        }

        try {
            Long catId = Long.parseLong(callbackData.split("_")[2]);
            CatRequestMessage request = new CatRequestMessage(
                CatOperationType.GET_CAT_BY_ID,
                new GetCatByIdPayload(catId),
                System.currentTimeMillis(),
                chatId
            );
            CatResponseMessage response = catKafkaService.sendRequest(request).get(5, TimeUnit.SECONDS);
            if ("OK".equals(response.getStatus()) && response.getPayload() instanceof SingleCatResponsePayload payload && payload.getCat() != null) {
                Cat cat = payload.getCat();
                sendCatDetails(cat, chatId, bot);
            } else {
                ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("error.cat.not_found"));
            }
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("error.cat.not_found"));
        }
    }

    private void sendCatDetails(Cat cat, Long chatId, ITelegramMessageWorker bot) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId.toString())
                    .photo(new InputFile(
                            new ByteArrayInputStream(cat.getPhotoData()),
                            "cat_" + cat.getId() + ".jpg"
                    ))
                    .caption(cat.getName())
                    .replyMarkup(createDetailsKeyboard(cat.getId()))
                    .build();

            bot.sendPhoto(sendPhoto);
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, MessageBundle.getMessage("error.cat.load"));
        }
    }

    private InlineKeyboardMarkup createDetailsKeyboard(Long catId) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.delete"))
                                        .callbackData("DELETE_CAT_" + catId)
                                        .build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text(MessageBundle.getMessage("button.back"))
                                        .callbackData(CallbackData.MYCATS_BACK.name())
                                        .build()
                        )
                ))
                .build();
    }
}
