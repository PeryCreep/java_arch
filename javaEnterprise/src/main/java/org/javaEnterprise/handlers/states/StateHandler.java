package org.javaEnterprise.handlers.states;

import org.javaEnterprise.services.UserDataFacade;
import org.telegram.telegrambots.meta.api.objects.Update;


public interface StateHandler {

    void handle(Update update, ITelegramMessageWorker bot, UserDataFacade userDataFacade);



    default StateHandler nextHandler() {
        return null;
    }
}
