package org.javaEnterprise.handlers.stateHanlers;

import org.javaEnterprise.handlers.states.StateHandler;
import org.javaEnterprise.handlers.states.UserState;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class HandlerProvider {
    
    private final BeanFactory beanFactory;
    private final Map<UserState, String> handlerBeanNames = new EnumMap<>(UserState.class);
    
    public HandlerProvider(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        initHandlerBeanNames();
    }

    private void initHandlerBeanNames() {
        handlerBeanNames.put(UserState.START, "startStateHandler");
        handlerBeanNames.put(UserState.AWAIT_NAME, "awaitNameHandler");
        handlerBeanNames.put(UserState.MAIN_MENU, "mainMenuHandler");
        handlerBeanNames.put(UserState.VIEW_MY_CATS, "viewMyCatsHandler");
        handlerBeanNames.put(UserState.ADD_CAT_IMAGE, "addCatImageStateHandler");
        handlerBeanNames.put(UserState.ADD_CAT_NAME, "addCatNameStateHandler");
        handlerBeanNames.put(UserState.ADD_CAT_SAVE, "addCatSaveStateHandler");
        handlerBeanNames.put(UserState.ADD_CAT_CONFIRM, "addCatConfirmStateHandler");
        handlerBeanNames.put(UserState.VIEW_RANDOM_CAT, "viewRandomCatHandler");
        handlerBeanNames.put(UserState.VIEW_CAT_DETAILS, "catDetailsHandler");
    }

    public StateHandler get(UserState state) {
        String beanName = handlerBeanNames.get(state);
        if (beanName == null) {
            throw new IllegalArgumentException("No handler registered for state: " + state);
        }
        return beanFactory.getBean(beanName, StateHandler.class);
    }
} 