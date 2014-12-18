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
    private final boolean staticReference;

    /**
     * Constructs a new reference to an event handler method.
     * @param argClassInstance The class instance of the handler method or <code>null</code> if the
     * handler method is static.
     * @param method The handler method. This cannot be <code>null</code>.
     */
    public MethodReference(Object argClassInstance, Method method) {
        assert method != null;

        // Suppress access checking on the handler method. Besides reducing VM overhead it also
        // allows handler methods to be private so that they aren't exposed to outside classes.
        method.setAccessible(true);

        try {
            this.classInstance = argClassInstance;
            this.methodHandle = MethodHandles.lookup().unreflect(method);
            this.staticReference = argClassInstance == null;
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
        try {
            // Get the proper invocation arguments from the arguments supplied by the caller.
            Object[] invokeArgs = getInvokeArguments(args);

            // Invoke the method handle with the invocation arguments. This is essentially doing
            // this: methodHandle(invokeArgs[0], invokeArgs[1], ..., invokeArgs[n]);
            MethodHandles.spreadInvoker(methodHandle.type(), 0).invoke(methodHandle, invokeArgs);
        } catch (Throwable e) {
            // Anything thrown by the handler will end up here. Event handlers should never
            // throw any exceptions, but there is no way to guarantee that. There is no way to
            // avoid catching as broad as 'Throwable' as the method invoke is defined as throws 
            // Throwable, which means we either catch it here and break a Checkstyle rule or define
            // this method as throws Throwable, which besides being messy (as the Throwable 
            // instance can be a checked or unchecked exception) would  just propagate the issue to
            // the caller. We'll just have to live with this one Checkstyle violation.
            // Throw a new unchecked exception so that the program blows up and the programmer/user
            // is informed something went wrong when it shouldn't have.
            throw new RuntimeException("Exception during event handler invocation: "
                    + e.getMessage(), e.getCause());
        }
    }

    private Object[] getInvokeArguments(Object[] args) {
        if (staticReference) {
            // If this is a static reference then the args array doesn't need any modifications.
            return args;
        } else {
            // An instanced reference will need some modifications however. The first argument of 
            // the InvokeArgs array is the class instance on which to invoke the method, any 
            // arguments given as a parameter will follow after.
            Object[] invokeArgs = new Object[args.length + 1];

            invokeArgs[0] = classInstance;
            System.arraycopy(args, 0, invokeArgs, 1, args.length);

            return invokeArgs;
        }
    }
}
