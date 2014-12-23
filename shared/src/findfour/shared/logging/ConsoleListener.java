package findfour.shared.logging;

public class ConsoleListener implements LogListener {

    @Override
    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void warning(String message) {
        System.out.printf("WARNING: %s\n", message);
    }

    @Override
    public void error(String message) {
        System.err.println(message);
    }

}
