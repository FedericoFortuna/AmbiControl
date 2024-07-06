package com.example.bluetooth.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StateMessage {
    private static StateMessage instance;
    private final Map<Integer, String> messageMap;

    private StateMessage() {
        messageMap = new HashMap<>();
        configure();
    }

    public static synchronized StateMessage getInstance() {
        if (instance == null) 
        {
            instance = new StateMessage();
        }
        return instance;
    }

    private void configure() {
        messageMap.put(0, Constants.TEXT_PREPARADO);
        messageMap.put(1, Constants.TEXT_ILUMINANDO_Y_VENTILANDO);
        messageMap.put(2, Constants.TEXT_DESENERGIZANDO_ACTUADORES);
        messageMap.put(3, Constants.TEXT_VENTILANDO);
        messageMap.put(4, Constants.TEXT_DESENERGIZANDO_VENTILADORES);
        messageMap.put(5, Constants.TEXT_VENTILANDO_POR_BT);
    }

    public String getValue(Integer key) {
        return Objects.requireNonNull(messageMap.get(key));
    }


}
