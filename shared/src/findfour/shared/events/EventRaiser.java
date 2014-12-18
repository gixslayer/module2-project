package findfour.shared.events;

public abstract class EventRaiser {
    protected final EventDispatcher dispatcher;

    public EventRaiser() {
        this.dispatcher = new EventDispatcher();
    }

    public void registerEventHandlers(Object instance) {
        dispatcher.registerEventHandlers(instance);
    }

    public void registerStaticEventHandlers(Class<?> type) {
        dispatcher.registerStaticEventHandlers(type);
    }
}
