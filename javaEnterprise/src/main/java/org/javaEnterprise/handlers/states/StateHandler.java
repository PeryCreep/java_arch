package org.javaEnterprise.handlers.states;

import org.javaEnterprise.controllers.CatsBot;
import org.javaEnterprise.services.UserSessionService;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface StateHandler {
    void handle(Update update, Long chatId, UserSessionService sessionService, CatsBot bot);
}
