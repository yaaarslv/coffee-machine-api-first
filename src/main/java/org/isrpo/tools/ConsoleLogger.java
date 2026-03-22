package org.isrpo.tools;

import lombok.Getter;
import org.isrpo.models.ConsoleColor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom logger
 */
public class ConsoleLogger {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Getter
    public enum LogLevel {
        INFO(ConsoleColor.GREEN),
        WARNING(ConsoleColor.YELLOW),
        ERROR(ConsoleColor.RED);

        private final ConsoleColor color;

        LogLevel(ConsoleColor color) {
            this.color = color;
        }

    }

    public static void log(String message, LogLevel level) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        String coloredMessage = level.getColor().colorize(String.format("[%s] [%s] %s", timestamp, level.name(), message));
        System.out.println(coloredMessage);
    }
}
