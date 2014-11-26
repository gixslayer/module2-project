package findfour.shared.events;

public class InvalidEventHandlerException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidEventHandlerException(String message) {
        super(message);
    }

    public InvalidEventHandlerException(String format, Object... args) {
        super(String.format(format, args));
    }

}
