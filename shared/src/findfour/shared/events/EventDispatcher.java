package findfour.shared.events;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import findfour.shared.ArgumentException;
import findfour.shared.ArgumentNullException;

public class EventDispatcher {
    private final Map<Integer, Event> events;
    private final Map<Integer, List<MethodHandle>> eventHandlers;
    private Object targetClass;

    public EventDispatcher() {
        this.events = new HashMap<Integer, Event>();
        this.eventHandlers = new HashMap<Integer, List<MethodHandle>>();
    }

    public void registerEvent(Event event) {
        if (event == null) {
            throw new ArgumentNullException("event");
        } else if (events.containsKey(event.getEventId())) {
            throw new ArgumentException("event", "An event with id %d is already registered",
                    event.getEventId());
        }

        events.put(event.getEventId(), event);
        eventHandlers.put(event.getEventId(), new ArrayList<MethodHandle>());
    }

    public void registerEventHandlers(Object argClass) throws InvalidEventHandlerException {
        targetClass = argClass;
        Class<?> classType = argClass.getClass();

        for (Method method : classType.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }

            EventHandler handler = method.getAnnotation(EventHandler.class);

            // Ensure that the handler's event id is a known event id for the dispatcher as there
            // is no point in registering an event handler for an event that will never be raised.
            if (!events.containsKey(handler.eventId())) {
                // Unknown event, throw exception.
                throw new InvalidEventHandlerException("Unknown event id %d.", handler.eventId());
            }

            Event event = events.get(handler.eventId());

            // Ensure that the handler's parameters are compatible with the parameters described by
            // the event. This is to prevent causing all kinds of invalid type cast exceptions when
            // trying to invoke the handler method once the event is raised.
            if (!validateArgTypes(method, event)) {
                throw new InvalidEventHandlerException(
                        "Handler parameters do not match the defined event parameters.");
            }

            // Suppress access checking on the handler method. Besides reducing VM overhead it also
            // allows handler methods to be private so that they aren't exposed to outside classes.
            method.setAccessible(true);

            // Try to get a direct method handle to the handler method.
            try {
                MethodHandle handle = MethodHandles.lookup().unreflect(method);

                eventHandlers.get(event.getEventId()).add(handle);
            } catch (IllegalAccessException e) {
                throw new InvalidEventHandlerException("Failed to access method: %s.",
                        e.getMessage());
            }
        }
    }

    private boolean validateArgTypes(Method method, Event event) {
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

    public void raiseEvent(int eventId, Object... args) {
        if (!events.containsKey(eventId)) {
            throw new ArgumentException("eventId", "Unknown event id %d", eventId);
        } else if (args == null) {
            throw new ArgumentNullException("args");
        } else if (!validateArgsForEvent(eventId, args)) {
            throw new ArgumentException("args", "Incompatible argument types for event id %d.",
                    eventId);
        }

        // Allocate an array in which the class instance reference and method arguments are stored.
        Object[] invokeArguments = new Object[args.length + 1];

        // Set the first element to the class instance reference on which the method will be
        // invoked. 
        invokeArguments[0] = targetClass;

        // After the class instance reference the actual method invocation parameters follow.
        for (int i = 0; i < args.length; i++) {
            invokeArguments[i + 1] = args[i];
        }

        for (MethodHandle handler : eventHandlers.get(eventId)) {
            try {
                // Invoke the method, this is essentially doing this:
                // targetClass.handler(args[0], args[1], ..., args[n]);
                handler.invokeWithArguments(invokeArguments);
            } catch (ClassCastException | WrongMethodTypeException e) {
                throw new ArgumentException("args", e.getMessage());
            } catch (Throwable e) {
                // Anything thrown by the handler will end up here. Event handlers should never
                // throw any exceptions, but there is no way to guarantee that. There is no way to
                // avoid catching as broad as 'Throwable' as the method invokeWithArguments is
                // defined as throws Throwable, which means we either catch it here and break a
                // Checkstyle rule or define this method as throws Throwable, which besides being
                // messy (as the Throwable instance can be a checked or unchecked exception) would 
                // just propagate the issue to the caller. We'll just have to live with this one
                // Checkstyle violation and hope it won't turn out to be a big deal. In case the
                // event handler did end up throwing an exception dump the stack trace to stderr.
                e.printStackTrace();
            }
        }
    }

    private boolean validateArgsForEvent(int eventId, Object[] args) {
        assert events.containsKey(eventId);

        Event event = events.get(eventId);
        Class<?>[] argTypes = event.getArgTypes();

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
}