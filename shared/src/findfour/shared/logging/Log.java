package findfour.shared.logging;

import java.util.ArrayList;
import java.util.List;

public class Log {
    private static final List<LogListener> LISTENERS = new ArrayList<LogListener>();
    private static LogLevel currentLevel;
    private static boolean debugEnabled;

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

    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    public static void debug(String message) {
        if (debugEnabled) {
            for (LogListener listener : LISTENERS) {
                listener.debug(message);
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

    /*@ pure */
    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void setLogLevel(LogLevel newLevel) {
        currentLevel = newLevel;
    }

    public static void setDebugEnabled(boolean value) {
        debugEnabled = value;
    }
}
