package findfour.shared.events;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Provides a direct access to an event handler method.
 * @author ciske
 *
 */
class MethodReference {
    private final Object classInstance;
    private final MethodHandle methodHandle;

    /**
     * Constructs a new reference to an event handler method.
     * @param argClassInstance The class instance of the handler method. This cannot be
     * <code>null</code>.
     * @param method The handler method. This cannot be <code>null</code>.
     */
    public MethodReference(Object argClassInstance, Method method) {
        assert argClassInstance != null;
        assert method != null;

        // Suppress access checking on the handler method. Besides reducing VM overhead it also
        // allows handler methods to be private so that they aren't exposed to outside classes.
        method.setAccessible(true);

        try {
            this.classInstance = argClassInstance;
            this.methodHandle = MethodHandles.lookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InvalidEventHandlerException("Failed access checking on handler method: %s.",
                    e.getMessage());
        }
    }

    /**
     * Invokes the event handler method.
     * @param args The arguments to pass onto the method.
     */
    public void invoke(Object... args) {
        // The first argument of the invokeArguments array is the class instance on which to invoke
        // the method, any arguments given as a parameter will follow after. This method will
        // effectively do this: classInstance.methodHandle(args[0], args[1], ..., args[n]);
        Object[] invokeArguments = new Object[args.length + 1];

        invokeArguments[0] = classInstance;
        System.arraycopy(args, 0, invokeArguments, 1, args.length);

        try {
            methodHandle.invokeWithArguments(invokeArguments);
        } catch (Throwable e) {
            // Anything thrown by the handler will end up here. Event handlers should never
            // throw any exceptions, but there is no way to guarantee that. There is no way to
            // avoid catching as broad as 'Throwable' as the method invokeWithArguments is
            // defined as throws Throwable, which means we either catch it here and break a
            // Checkstyle rule or define this method as throws Throwable, which besides being
            // messy (as the Throwable instance can be a checked or unchecked exception) would 
            // just propagate the issue to the caller. We'll just have to live with this one
            // Checkstyle violation and hope it won't turn out to be a big deal. There is nothing
            // to do as to handling the exception. We'll just print the stack trace to the stderr
            // so that programmer hopefully notices he screwed up somewhere instead of blowing the
            // whole application up (even though that might not even be a bad decision as things
            // are bound to go horribly wrong).
            System.err.println("Exception while invoking method handler");
            e.printStackTrace();
        }
    }
}
