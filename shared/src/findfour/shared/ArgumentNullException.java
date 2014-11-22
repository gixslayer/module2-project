package findfour.shared;

/**
 * Represents an exception which can be thrown at runtime to indicate a method has been called with
 * a <code>null</code> argument.
 * @author ciske
 * 
 */
public class ArgumentNullException extends ArgumentException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>ArgumentNullException</code> instance.
     * @param param The name of the <code>null</code> argument.
     */
    public ArgumentNullException(String param) {
        super(param, "value is null");
    }
}
