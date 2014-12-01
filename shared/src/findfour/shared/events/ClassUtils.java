package findfour.shared.events;


/**
 * Utility class for certain class related operations.
 * @author ciske
 * 
 */
class ClassUtils {
    /**
     * Returns the wrapped type of a primitive type.
     * @param primitiveClass The primitive class type.
     * @return the wrapped type of the primitive type.
     */
    public static Class<?> getWrapperClass(Class<?> primitiveClass) {
        assert primitiveClass != null;
        assert primitiveClass.isPrimitive();

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

        // Should never be reached unless another primitive type is added to Java.
        return null;
    }
}
