package findfour.shared.events;

/**
 * Defines an exception which is thrown when an event handler could not be validated.
 * @author ciske
 * 
 */
public class InvalidEventHandlerException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>InvalidEventHandlerException</code> instance.
     * @param message The message of the exception that describes why the event handler could not be
     * validated.
     */
    public InvalidEventHandlerException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>InvalidEventHandlerException</code> instance.
     * @param format The format of that describes why the event handler could not be validated.
     * @param args The format parameters.
     */
    public InvalidEventHandlerException(String format, Object... args) {
        super(String.format(format, args));
    }

}
