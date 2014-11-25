package client;

import findfour.shared.events.Event;
import findfour.shared.events.EventDispatcher;
import findfour.shared.events.EventHandler;
import findfour.shared.events.InvalidEventHandlerException;

public class Main {

    private static EventDispatcher dispatcher;

    public static void main(String[] args) {
        dispatcher = new EventDispatcher();
        dispatcher.registerEvent(new Event(0, String.class, String.class, int.class));
        try {
            dispatcher.registerEventHandlers(new Main());

            dispatcher.raiseEvent(0, "Hello", "Bob", 4);
        } catch (InvalidEventHandlerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @EventHandler(eventId = 0)
    private void testHandler(String arg1, String arg2, int a) {
        System.out.println(arg1 + arg2);
        System.out.println(a);
    }
}
