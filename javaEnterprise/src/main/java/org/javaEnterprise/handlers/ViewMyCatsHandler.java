package org.javaEnterprise.handlers;

import org.javaEnterprise.domain.Cat;
import org.javaEnterprise.domain.User;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.ITelegramMessageWorker;
import org.javaEnterprise.services.CatService;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.UserService;
import org.javaEnterprise.services.enums.CallbackData;
import org.javaEnterprise.util.ErrorHandler;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
    public void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().startsWith("DELETE_CAT_")) {
            handleDeleteCat(update, bot, userDataFacade);
            return;
        }

        try {
            User user = userService.findByChatId(chatId)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            int page = getCurrentPage(chatId, userDataFacade);
            Page<Cat> catPage = catService.getCatsByAuthor(user.getId(), page, PAGE_SIZE);

            if (catPage.isEmpty()) {
                sendEmptyMessage(chatId, bot);
            } else {
                sendCatsPage(chatId, catPage, bot, userDataFacade);
            }
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, e);
        }
    }

    private void handleDeleteCat(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        Long catId = Long.parseLong(update.getCallbackQuery().getData().split("_")[2]);
        
        try {
            User user = userService.findByChatId(chatId).orElseThrow();
            catService.deleteCat(catId, user.getId());
            editMessageAfterDelete(update, bot);
            
            int page = getCurrentPage(chatId, userDataFacade);
            Page<Cat> catPage = catService.getCatsByAuthor(user.getId(), page, PAGE_SIZE);
            
            if (catPage.isEmpty() && page > 0) {
                userDataFacade.storePage(chatId, page - 1);
                handle(update, bot, userDataFacade);
            } else {
                if (catPage.isEmpty()) {
                    sendEmptyMessage(chatId, bot);
                } else {
                    sendCatsPage(chatId, catPage, bot, userDataFacade);
                }
            }
        } catch (Exception e) {
            ErrorHandler.handleError(chatId, bot, e);
        }
    }

    private void editMessageAfterDelete(Update update, ITelegramMessageWorker bot) {
        EditMessageText editMessage = EditMessageText.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .text(MessageBundle.getMessage("view.cat.delete.success"))
                .build();

        bot.editMessage(editMessage);
    }

    private int getCurrentPage(Long chatId, UserDataFacade userDataFacade) {
        Integer page = userDataFacade.getPage(chatId);
        return page != null ? page : 0;
    }

    private void sendCatsPage(Long chatId, Page<Cat> catPage, ITelegramMessageWorker bot, UserDataFacade userDataFacade) {
        userDataFacade.storePage(chatId, catPage.getNumber());
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
                        .callbackData(CallbackData.MAIN_MENU.name())
                        .build()
        ));

        return keyboard;
    }

    private List<InlineKeyboardButton> createPaginationButtons(Page<Cat> catPage) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        if (catPage.hasPrevious()) {
            buttons.add(InlineKeyboardButton.builder()
                    .text(MessageBundle.getMessage("view.my.cat.back"))
                    .callbackData("MYCATS_PAGE_" + (catPage.getNumber() - 1))
                    .build());
        }

        if (catPage.hasNext()) {
            buttons.add(InlineKeyboardButton.builder()
                    .text(MessageBundle.getMessage("view.my.cat.next"))
                    .callbackData("MYCATS_PAGE_" + (catPage.getNumber() + 1))
                    .build());
        }

        return buttons;
    }

    private void sendEmptyMessage(Long chatId, ITelegramMessageWorker bot) {
        bot.sendMessage(SendMessage.builder()
                .chatId(chatId)
                .text(MessageBundle.getMessage("view.my.cats.empty"))
                .build());
    }
}