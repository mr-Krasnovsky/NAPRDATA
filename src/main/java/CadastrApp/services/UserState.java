package CadastrApp.services;

import CadastrApp.config.AppConstants;

import java.util.HashMap;
import java.util.Map;

public class UserState {
    public static final int NONE = 0;
    public static final int WAITING_FOR_CADASTRAL_NUMBER = 1;
    public static final int WAITING_FOR_UTM_SELECTION = 2;
    public static final int WAITING_FOR_FORMAT_SELECTION = 3;

    private static final Map<String, Integer> userStates = new HashMap<>();

    public static void setState(String chatId, int state) {
        userStates.put(chatId, state);
    }

    public static int getState(String chatId) {
        return userStates.getOrDefault(chatId, NONE);
    }
}
