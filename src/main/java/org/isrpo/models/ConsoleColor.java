package org.isrpo.models;

import lombok.Getter;

/**
 * Enum of color codes for custom logging
 */
@Getter
public enum ConsoleColor {
    RESET("\033[0m"),
    RED("\033[31m"),
    GREEN("\033[32m"),
    YELLOW("\033[33m"),
    BLUE("\033[34m"),
    PURPLE("\033[35m"),
    CYAN("\033[36m");

    private final String code;

    ConsoleColor(String code) {
        this.code = code;
    }

    public String colorize(String message) {
        return code + message + RESET.code;
    }
}
