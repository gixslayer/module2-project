package findfour.shared.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an annotation which can be applied on methods to indicate they should be called when the
 * corresponding event is raised. An event handler should never throw an exception.
 * @author ciske
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * The unique identifier of the event to handle.
     */
    int eventId();
}
