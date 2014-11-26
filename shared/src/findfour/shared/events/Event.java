package findfour.shared.events;

import findfour.shared.ArgumentNullException;

/**
 * Represents an event definition.
 * @author ciske
 * 
 */
public class Event {
    private final int eventId;
    private final Class<?>[] argTypes;

    /**
     * Creates a new <code>EVent</code> instance.
     * @param argEventId The unique identifier of this <code>Event</code>.
     * @param args The parameter types of this <code>Event</code>.
     */
    public Event(int argEventId, Class<?>... args) {
        if (args == null) {
            throw new ArgumentNullException("args");
        }

        this.eventId = argEventId;
        this.argTypes = args;
    }

    /**
     * Returns the unique identifier of this <code>Event</code>.
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Returns the array of types this <code>Event</code> has as its parameters.
     */
    public Class<?>[] getArgTypes() {
        return argTypes;
    }
}
