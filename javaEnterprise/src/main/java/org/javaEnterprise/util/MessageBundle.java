package org.javaEnterprise.util;

import java.util.ResourceBundle;

public class MessageBundle {
    private static final ResourceBundle bundle = ResourceBundle.getBundle("messages");

    public static String getMessage(String key) {
        return bundle.getString(key);
    }
} 