package findfour.shared.logging;

public class ConsoleListener implements LogListener {

    @Override
    public synchronized void info(String message) {
        System.out.println(message);
    }

    @Override
    public synchronized void warning(String message) {
        System.out.printf("WARNING: %s\n", message);
    }

    @Override
    public synchronized void error(String message) {
        System.err.println(message);
    }

    @Override
    public synchronized void debug(String message) {
        System.out.printf("DEBUG: %s\n", message);
    }
}
