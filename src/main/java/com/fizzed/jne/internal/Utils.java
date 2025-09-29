package com.fizzed.jne.internal;

public class Utils {

    static public String trimToNull(String value) {
        if (value != null) {
            value = value.trim();
            if (value.equals("")) {
                return null;
            }
        }
        return value;
    }

}