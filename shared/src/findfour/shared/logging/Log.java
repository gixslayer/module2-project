package findfour.shared.logging;

import java.util.ArrayList;
import java.util.List;

public class Log {
    private static final List<LogListener> LISTENERS = new ArrayList<LogListener>();
    private static LogLevel currentLevel;

    public static void info(LogLevel level, String format, Object... args) {
        info(level, String.format(format, args));
    }

    public static void info(LogLevel level, String message) {
        if (currentLevel.getLevel() >= level.getLevel()) {
            for (LogListener listener : LISTENERS) {
                listener.info(message);
            }
        }
    }

    public static void warning(LogLevel level, String format, Object... args) {
        warning(level, String.format(format, args));
    }

    public static void warning(LogLevel level, String message) {
        if (currentLevel.getLevel() >= level.getLevel()) {
            for (LogListener listener : LISTENERS) {
                listener.warning(message);
            }
        }
    }

    public static void error(LogLevel level, String format, Object... args) {
        error(level, String.format(format, args));
    }

    public static void error(LogLevel level, String message) {
        if (currentLevel.getLevel() >= level.getLevel()) {
            for (LogListener listener : LISTENERS) {
                listener.error(message);
            }
        }
    }

    public static void registerListener(LogListener listener) {
        LISTENERS.add(listener);
    }

    public static void unregisterListener(LogListener listener) {
        if (LISTENERS.contains(listener)) {
            LISTENERS.remove(listener);
        }
    }

    public static LogLevel getLogLevel() {
        return currentLevel;
    }

    public static void setLogLevel(LogLevel newLevel) {
        currentLevel = newLevel;
    }
}
