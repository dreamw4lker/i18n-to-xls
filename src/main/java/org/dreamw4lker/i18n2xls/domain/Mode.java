package org.dreamw4lker.i18n2xls.domain;

import lombok.Getter;

/**
 * Enum с режимами запуска приложения
 *
 * @author Alexander Shkirkov
 */
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
