package br.net.dd.netherwingcore.common;

import java.util.HashMap;
import java.util.Map;

public class AnyData {

    private Map<String, Object> dataMap = new HashMap<>();

    public <T> void set(String key, T value) {
        dataMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, T defaultValue) {
        return (T) dataMap.getOrDefault(key, defaultValue);
    }

    public boolean exists(String key) {
        return dataMap.containsKey(key);
    }

    public void remove(String key) {
        dataMap.remove(key);
    }

    public int increment(String key, int increment) {
        int currentValue = getValue(key, 0);
        currentValue += increment;
        set(key, currentValue);
        return currentValue;
    }

    public boolean incrementOrReset(String key, int maxValue, int increment) {
        int newValue = increment(key, increment);
        if (newValue < maxValue) {
            return false;
        }
        remove(key);
        return true;
    }

}
