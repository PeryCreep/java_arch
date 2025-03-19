package org.javaEnterprise.configs;

import org.javaEnterprise.handlers.*;
import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class BotConfig {

    @Bean
    public Map<UserState, StateHandler> stateHandlers(
            StartStateHandler startHandler,
            MainMenuHandler mainMenuHandler,
            ViewMyCatsHandler viewMyCatsHandler,
            AddCatImageStateHandler addCatImageHandler,
            ViewRandomCatHandler randomCatHandler,
            AwaitNameHandler awaitNameHandler
    ) {
        Map<UserState, StateHandler> map = new EnumMap<>(UserState.class);
        map.put(UserState.START, startHandler);
        map.put(UserState.AWAIT_NAME, awaitNameHandler);
        map.put(UserState.MAIN_MENU, mainMenuHandler);
        map.put(UserState.VIEW_MY_CATS, viewMyCatsHandler);
        map.put(UserState.ADD_CAT_IMAGE, addCatImageHandler);
        map.put(UserState.VIEW_RANDOM_CAT, randomCatHandler);
        return map;
    }
}
