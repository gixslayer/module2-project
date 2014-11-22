package findfour.shared.game;

import findfour.shared.ArgumentException;

/**
 * Represents the possible states a disc can have.
 * @author ciske
 * 
 */
public enum Disc {
    /**
     * A disk owned by the red player.
     */
    Red(0),
    /**
     * A disk owned by the yellow player.
     */
    Yellow(1),
    /**
     * Represents the absence of a disk.
     */
    None(2);

    private final byte value;

    private Disc(int argValue) {
        this.value = (byte) argValue;
    }

    /**
     * Returns the unique value of the <code>Disc</code>.
     */
    public byte getValue() {
        return value;
    }

    /**
     * Checks if the specified value is mapped to a <code>Disc</code>.
     * @param value The value to be checked.
     * @return <code>true</code> if the value is valid. Otherwise <code>false</code> is returned
     */
    public static boolean isValid(byte value) {
        return value == Red.value || value == Yellow.value || value == None.value;
    }

    /**
     * Returns the <code>Disc</code> mapped to the specified value.
     * @param value A valid value that is linked to a <code>Disc</code>.
     * @return the <code>Disc</code> mapped to the specified value
     */
    public static Disc fromValue(byte value) {
        if (!isValid(value)) {
            throw new ArgumentException("value", "%d is not mapped to a Disc.", value);
        }

        if (value == Red.value) {
            return Red;
        } else if (value == Yellow.value) {
            return Yellow;
        } else { /* value == None.value */
            return None;
        }
    }
}
