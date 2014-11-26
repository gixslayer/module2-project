package findfour.shared.events;

import findfour.shared.ArgumentException;
import findfour.shared.ArgumentNullException;

/**
 * Utility class for certain class related operations.
 * @author ciske
 * 
 */
public class ClassUtils {
    /**
     * Returns the wrapped type of a primitive type.
     * @param primitiveClass The primitive class type.
     * @return the wrapped type of the primitive type.
     */
    public static Class<?> getWrapperClass(Class<?> primitiveClass) {
        if (primitiveClass == null) {
            throw new ArgumentNullException("primitiveClass");
        } else if (!primitiveClass.isPrimitive()) {
            throw new ArgumentException("primitiveClass", "Not a primitive");
        }

        if (primitiveClass.equals(byte.class)) {
            return Byte.class;
        } else if (primitiveClass.equals(boolean.class)) {
            return Boolean.class;
        } else if (primitiveClass.equals(short.class)) {
            return Short.class;
        } else if (primitiveClass.equals(int.class)) {
            return Integer.class;
        } else if (primitiveClass.equals(long.class)) {
            return Long.class;
        } else if (primitiveClass.equals(char.class)) {
            return Character.class;
        } else if (primitiveClass.equals(float.class)) {
            return Float.class;
        } else if (primitiveClass.equals(double.class)) {
            return Double.class;
        }

        // Should never be reached.
        return null;
    }
}
