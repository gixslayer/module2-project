package findfour.shared.logging;

public interface LogListener {
    void info(String message);

    void warning(String message);

    void error(String message);

    void debug(String message);
}
