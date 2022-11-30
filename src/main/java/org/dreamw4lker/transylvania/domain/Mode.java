package org.dreamw4lker.transylvania.domain;

import lombok.Getter;

public enum Mode {
    JSON_TO_XLS("json2xls"),
    XLS_TO_JSON("xls2json");

    @Getter
    private final String text;

    Mode(String text) {
        this.text = text;
    }

    public static Mode fromString(String text) {
        for (Mode mode : Mode.values()) {
            if (mode.text.equalsIgnoreCase(text)) {
                return mode;
            }
        }
        return null;
    }
}
