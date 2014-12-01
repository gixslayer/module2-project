package findfour.shared.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event, which once invoked will be send to all registered event handlers.
 * @author ciske
 * 
 */
class Event {
    private final int id;
    private final Class<?>[] argTypes;
    private final List<MethodReference> handlers;

    /**
     * Creates a new <code>Event</code> instance with one or more arguments.
     * @param argId The unique identifier of this <code>Event</code>.
     * @param args The argument types of this <code>Event</code>. This cannot be <code>null</code>.
     */
    public Event(int argId, Class<?>... args) {
        assert args != null;

        this.id = argId;
        this.argTypes = args;
        this.handlers = new ArrayList<MethodReference>();
    }

    /**
     * Creates a new <code>Event</code> instance without any arguments.
     * @param argId The unique identifier of this <code>Event</code>.
     */
    public Event(int argId) {
        this(argId, new Class<?>[0]);
    }

    /**
     * Registers a new event handler to this <code>Event</code>.
     * @param handler A reference to the method that will handle the event. This cannot be
     * <code>null</code>.
     */
    public void addHandler(MethodReference handler) {
        assert handler != null;

        handlers.add(handler);
    }

    /**
     * Invokes all registered handlers with the given arguments.
     * @param args The arguments to pass to the handlers. This cannot be <code>null</code>.
     */
    public void invokeHandlers(Object... args) {
        assert args != null;

        for (MethodReference handler : handlers) {
            handler.invoke(args);
        }
    }

    /**
     * Validates the specified args to the <code>Event</code> arguments to ensure they are
     * compatible.
     * @param args The arguments to validate. This cannot be <code>null</code>.
     * @return <code>true</code> if the arguments are compatible, otherwise <code>false</code> is
     * returned
     */
    public boolean validateArgs(Object[] args) {
        assert args != null;

        // Make sure the raised event and the handler have the same number of arguments.
        if (args.length != argTypes.length) {
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            // The type as defined in the event.
            Class<?> eventType = argTypes[i];
            // The type of the corresponding argument.
            Class<?> argType = args[i].getClass();

            if (eventType.isPrimitive()) {
                // Primitives cause an issue as they are boxed into objects (EG: int to
                // java.lang.Integer). Compare to the wrapped type instead.
                eventType = ClassUtils.getWrapperClass(eventType);
            }

            // Ensure the argument type can be casted to the defined event type.
            if (!eventType.isAssignableFrom(argType)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the unique identifier of this <code>Event</code>.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the array of types this <code>Event</code> has as its arguments.
     */
    public Class<?>[] getArgTypes() {
        return argTypes;
    }
}
