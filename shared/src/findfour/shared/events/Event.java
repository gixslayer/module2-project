package findfour.shared.events;

import findfour.shared.ArgumentNullException;

public class Event {
    private final int eventId;
    private final Class<?>[] argTypes;

    public Event(int argEventId, Class<?>... args) {
        if (args == null) {
            throw new ArgumentNullException("args");
        }

        this.eventId = argEventId;
        this.argTypes = args;
    }

    public int getEventId() {
        return eventId;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }
}
