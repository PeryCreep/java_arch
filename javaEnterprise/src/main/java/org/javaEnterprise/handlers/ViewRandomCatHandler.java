package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

@Component
public class ViewRandomCatHandler implements StateHandler {

    private final CatService catService;

    public ViewRandomCatHandler(CatService catService) {
        this.catService = catService;
    }

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        Optional<Cat> catOpt = catService.getRandomCat();
        if (catOpt.isEmpty()) {
            bot.sendMessage(SendMessage.builder().chatId(chatId)
                    .text(MessageBundle.getMessage("error.no.cats"))
                    .build());
            return;
        }
        Cat cat = catOpt.get();
        sendCatWithButtons(cat, chatId, bot);

    }

    private void sendCatWithButtons(Cat cat, Long chatId, CatsBot bot) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId.toString())
                    .photo(new InputFile(new ByteArrayInputStream(cat.getPhotoData()), "cat.jpg"))
                    .caption(String.format(
                            "%s\n%s: %s",
                            cat.getName(),
                            MessageBundle.getMessage("cat.author"),
                            cat.getAuthor().getName()))
                    .replyMarkup(createRatingKeyboard(cat))
                    .build();

            bot.sendPhoto(sendPhoto);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            bot.sendMessage(SendMessage.builder()
                    .chatId(chatId)
                    .text(MessageBundle.getMessage("error.cat.send"))
                    .build());
        }
    }

    private InlineKeyboardMarkup createRatingKeyboard(Cat cat) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                createRatingButton("LIKE", "👍", cat.getLikesCount(), cat.getId()),
                                createRatingButton("DISLIKE", "👎", cat.getDislikesCount(), cat.getId())
                        ),
                        List.of(createBackButton())
                ))
                .build();
    }

    private InlineKeyboardButton createRatingButton(String type, String emoji, int count, Long catId) {
        return InlineKeyboardButton.builder()
                .text("%s (%d)".formatted(emoji, count))
                .callbackData("%s_%d".formatted(type, catId))
                .build();
    }

    private InlineKeyboardButton createBackButton() {
        return InlineKeyboardButton.builder()
                .text(MessageBundle.getMessage("button.back"))
                .callbackData("MAIN_MENU")
                .build();
    }
}
