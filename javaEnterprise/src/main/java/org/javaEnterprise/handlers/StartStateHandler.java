package org.javaEnterprise.handlers;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.javaEnterprise.services.UserService;
import org.javaEnterprise.util.MessageBundle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class StartStateHandler implements StateHandler {
    private final UserService userService;

    public StartStateHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(Update update, CatsBot bot) {
        Long chatId = bot.getChatId(update);
        if (chatId == null) return;

        if (userService.isUserExists(chatId)) {
            bot.setState(chatId, UserState.MAIN_MENU);
            StateHandler mainMenuHandler = bot.getHandlers().get(UserState.MAIN_MENU);
            mainMenuHandler.handle(update, bot);
            return;
        }

        SendMessage welcomeMessage = new SendMessage();
        welcomeMessage.setChatId(chatId.toString());
        welcomeMessage.setText(MessageBundle.getMessage("view.hi"));
        bot.sendMessage(welcomeMessage);
        
        bot.setState(chatId, UserState.AWAIT_NAME);
        
        SendMessage namePrompt = new SendMessage();
        namePrompt.setChatId(chatId.toString());
        namePrompt.setText(MessageBundle.getMessage("await.name.prompt"));
        bot.sendMessage(namePrompt);
    }
}
