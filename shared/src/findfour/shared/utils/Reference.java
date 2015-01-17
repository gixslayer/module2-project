package findfour.shared.utils;

public class Reference<T> {
    private T value;

    public Reference() {
        /*
         * Java does not support default values for generic types so just leave it unassigned in 
         * the default constructor.
         */
    }

    public Reference(T defaultValue) {
        this.value = defaultValue;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        value = newValue;
    }
}
