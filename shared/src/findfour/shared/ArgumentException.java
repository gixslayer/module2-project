package findfour.shared;

/**
 * Represents an exception which can be thrown at runtime to indicate a method has been called with
 * an invalid argument.
 * @author ciske
 * 
 */
public class ArgumentException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <Code>ArgumentException</code> instance.
     * @param param The name of the argument.
     * @param message The message that explains why the argument is invalid.
     */
    public ArgumentException(String param, String message) {
        super(String.format("%s: %s.", param, message));
    }

    /**
     * Creates a new <Code>ArgumentException</code> instance.
     * @param param The name of the argument.
     * @param format The format of the message that explains why the argument is invalid.
     * @param args The vararg format arguments.
     */
    public ArgumentException(String param, String format, Object... args) {
        this(param, String.format(format, args));
    }
}
