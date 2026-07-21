package com.ticketing.global.util;


public final class NameMasker {

    private static final char MASK = '*';

    private NameMasker() {
    }


    public static String mask(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }

        int length = name.length();
        if (length == 1) {
            return String.valueOf(MASK);
        }
        if (length == 2) {
            return name.charAt(0) + String.valueOf(MASK);
        }

        return name.charAt(0)
                + String.valueOf(MASK).repeat(length - 2)
                + name.charAt(length - 1);
    }
}
