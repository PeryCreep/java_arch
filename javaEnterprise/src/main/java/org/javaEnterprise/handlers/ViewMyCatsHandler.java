package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.services.UserService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class ViewMyCatsHandler implements StateHandler {
    private static final int PAGE_SIZE = 9;
    private final CatService catService;
    private final UserService userService;

    public ViewMyCatsHandler(CatService catService,
                             UserService userService) {
        this.catService = catService;
        this.userService = userService;
    }

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        User user = userService.findByChatId(chatId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        int page = getCurrentPage(chatId, bot);
        Page<Cat> catPage = catService.getCatsByAuthor(user.getId(), page, PAGE_SIZE);

        if (catPage.isEmpty()) {
            sendEmptyMessage(chatId, bot);
        } else {
            sendCatsPage(chatId, catPage, bot);
        }
    }

    private int getCurrentPage(Long chatId, CatsBot bot) {
        Integer page = bot.getTempData(chatId, "myCatsPage", Integer.class);
        return page != null ? page : 0;
    }

    private void sendCatsPage(Long chatId, Page<Cat> catPage, CatsBot bot) {

        bot.storeTempData(chatId, "myCatsPage", catPage.getNumber());
        String caption = String.format(MessageBundle.getMessage("view.my.cats.page"),
                catPage.getNumber() + 1,
                catPage.getTotalPages());

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(createKeyboard(catPage))
                .build();

        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(caption)
                .replyMarkup(keyboard)
                .build());
    }

    private List<List<InlineKeyboardButton>> createKeyboard(Page<Cat> catPage) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        catPage.getContent().forEach(cat -> {
            keyboard.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(cat.getName())
                            .callbackData("VIEW_CAT_" + cat.getId())
                            .build()
            ));
        });

        if (catPage.getTotalPages() > 1) {
            keyboard.add(createPaginationButtons(catPage));
        }

        keyboard.add(List.of(
                InlineKeyboardButton.builder()
                        .text(MessageBundle.getMessage("button.back"))
                        .callbackData("MAIN_MENU")
                        .build()
        ));

        return keyboard;
    }

    private List<InlineKeyboardButton> createPaginationButtons(Page<Cat> catPage) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (catPage.hasPrevious()) {
            buttons.add(InlineKeyboardButton.builder()
                    .text("◀️")
                    .callbackData("MYCATS_PAGE_" + (catPage.getNumber() - 1))
                    .build());
        }

        if (catPage.hasNext()) {
            buttons.add(InlineKeyboardButton.builder()
                    .text("▶️")
                    .callbackData("MYCATS_PAGE_" + (catPage.getNumber() + 1))
                    .build());
        }

        return buttons;
    }

    private void sendEmptyMessage(Long chatId, CatsBot bot) {
        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("view.my.cats.empty"))
                .build());
    }
}