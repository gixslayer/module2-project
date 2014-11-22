package findfour.shared;

/**
 * Represents an exception which can be thrown at runtime to indicate a method has been called with
 * an argument outside the valid range.
 * @author ciske
 * 
 */
public class ArgumentOutOfRangeException extends ArgumentException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>ArgumentOutOfRangeException</code> instance.
     * @param param The name of the argument.
     * @param minValue The inclusive lower bound of the valid range.
     * @param maxValue The exclusive upper bound of the valid range.
     */
    public ArgumentOutOfRangeException(String param, Object minValue, Object maxValue) {
        super(param, "value must be within the range of [%s, %s)", minValue, maxValue);
    }

}
