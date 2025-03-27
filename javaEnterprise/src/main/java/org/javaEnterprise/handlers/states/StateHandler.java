package org.javaEnterprise.handlers.states;

import org.javaEnterprise.controllers.CatsBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface StateHandler {
    void handle(Update update, CatsBot bot);
}
