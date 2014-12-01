package findfour.shared.events;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import findfour.shared.ArgumentException;
import findfour.shared.ArgumentNullException;

/**
 * Defines a class on which events can be registered and raised.
 * @author ciske
 * 
 */
public class EventDispatcher {
    private final Map<Integer, Event> events;

    /**
     * Creates a new <code>EventDispatcher</code> instance with no registered events.
     */
    public EventDispatcher() {
        this.events = new HashMap<Integer, Event>();
    }

    /**
     * Registers a new event in the event dispatcher.
     * @param eventId The event's unique identifier which cannot already be registered.
     * @param argTypes The event's argument types. This cannot be <code>null</code>.
     */
    public void registerEvent(int eventId, Class<?>... argTypes) {
        if (argTypes == null) {
            throw new ArgumentNullException("argTypes");
        } else if (events.containsKey(eventId)) {
            throw new ArgumentException("eventId", "An event with id %d is already registered",
                    eventId);
        }

        events.put(eventId, new Event(eventId, argTypes));
    }

    /**
     * Registers all event handlers found in the specified class instance to this
     * <code>EventDispatcher</code> instance.
     * @param argClass The instance of the class. This cannot be <code>null</code>.
     */
    public void registerEventHandlers(Object argClass) {
        Class<?> classType = argClass.getClass();

        for (Method method : classType.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }

            EventHandler handler = method.getAnnotation(EventHandler.class);
            int eventId = handler.eventId();

            // Ensure that the handler's event id is a known event id for the dispatcher as there
            // is no point in registering an event handler for an event that will never be raised.
            if (!events.containsKey(eventId)) {
                // Unknown event, throw exception.
                throw new InvalidEventHandlerException("Unknown event id %d.", eventId);
            }

            Event event = events.get(eventId);

            // Ensure that the handler's arguments are compatible with the arguments described by
            // the event. This is to prevent causing all kinds of invalid type cast exceptions when
            // trying to invoke the handler method once the event is raised.
            if (!validateArgTypes(method, event)) {
                throw new InvalidEventHandlerException(
                        "Handler arguments do not match the defined event arguments.");
            }

            // Get a direct reference to the handler method and register it on the event.
            MethodReference methodReference = new MethodReference(argClass, method);
            event.addHandler(methodReference);
        }
    }

    /**
     * Validates that the parameters of a method are compatible with the arguments of an event.
     * @param method The method. This cannot be <code>null</code>.
     * @param event The event. This cannot be <code>null</code>.
     * @return <code>true</code> if the method and event are compatible, otherwise
     * <code>false</code> is returned
     */
    private boolean validateArgTypes(Method method, Event event) {
        assert method != null;
        assert event != null;

        Class<?>[] eventTypes = event.getArgTypes();
        Class<?>[] methodTypes = method.getParameterTypes();

        // Ensure the handler method and the event have the same number of parameters.
        if (methodTypes.length != eventTypes.length) {
            return false;
        }

        // Ensure each event parameter can be cast to the handler method's parameter.
        for (int i = 0; i < eventTypes.length; i++) {
            Class<?> eventType = eventTypes[i];
            Class<?> methodType = methodTypes[i];

            if (!eventType.isAssignableFrom(methodType)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Raises an event with one or more arguments which is send to all registered handlers.
     * @param eventId The event identifier which must be registered.
     * @param args The arguments of the event. This cannot be <code>null</code>. The argument types
     * must also match the argument types of the event.
     */
    public void raiseEvent(int eventId, Object... args) {
        if (args == null) {
            throw new ArgumentNullException("args");
        } else if (!events.containsKey(eventId)) {
            throw new ArgumentException("eventId", "Unknown event id %d", eventId);
        }

        Event event = events.get(eventId);

        if (!event.validateArgs(args)) {
            throw new ArgumentException("args",
                    "Incompatible argument types for event with id %d.", eventId);
        }

        event.invokeHandlers(args);
    }

    /**
     * Raises an event with no arguments which is send to all registered handlers.
     * @param eventId The event identifier which must be registered.
     */
    public void raiseEvent(int eventId) {
        raiseEvent(eventId, new Object[0]);
    }

}