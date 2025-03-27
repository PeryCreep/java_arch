package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserDataFacade;
import org.javaEnterprise.services.UserService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartStateHandler implements StateHandler {
    private final UserService userService;

    private final HandlerProvider handlerProvider;

    public StartStateHandler(UserService userService,HandlerProvider handlerProvider) {
        this.userService = userService;
        this.handlerProvider = handlerProvider;
    }

    @Override
    public void handle(Update update, CatsBot bot, UserDataFacade userDataFacade) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (userService.isUserExists(chatId)) {
            userDataFacade.setState(chatId, UserState.MAIN_MENU);
            StateHandler mainMenuHandler = nextHandler();
            mainMenuHandler.handle(update, bot, userDataFacade);
            return;
        }

        SendMessage welcomeMessage = new SendMessage();
        welcomeMessage.setChatId(chatId.toString());
        welcomeMessage.setText(MessageBundle.getMessage("view.hi"));
        bot.sendMessage(welcomeMessage);
        
        userDataFacade.setState(chatId, UserState.AWAIT_NAME);
    }

    @Override
    public StateHandler nextHandler() {
        return handlerProvider.get(UserState.MAIN_MENU);
    }
}
