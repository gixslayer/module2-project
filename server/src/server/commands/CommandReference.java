package server.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;
import findfour.shared.utils.Reference;
import findfour.shared.utils.StringUtils;

class CommandReference {
    private final Object targetInstance;
    private final Method targetMethod;
    private final Class<?>[] parameterTypes;

    CommandReference(Object instance, Method method) {
        this.targetInstance = instance;
        this.targetMethod = method;
        this.parameterTypes = method.getParameterTypes();

        targetMethod.setAccessible(true);
    }

    void invoke(String[] args) {
        if (args.length < parameterTypes.length) {
            Log.error(LogLevel.Minimal,
                    "failed to invoke command: expected %d args,  but found %d",
                    parameterTypes.length, args.length);
            return;
        }

        Object[] invokeArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            if (parameterTypes[i].equals(int.class)) {
                Reference<Integer> result = new Reference<Integer>();

                if (StringUtils.tryParseInt(args[i], result)) {
                    invokeArgs[i] = result.get();
                } else {
                    Log.error(LogLevel.Minimal,
                            "failed to parse value (%s) as a valid integer at parameter %d",
                            args[i], i);
                    return;
                }
            } else if (parameterTypes[i].equals(String.class)) {
                invokeArgs[i] = args[i];
            } else if (parameterTypes[i].equals(boolean.class)) {
                if (!args[i].matches("true|false")) {
                    Log.error(LogLevel.Minimal,
                            "failed to parse value (%s) as a valid boolean at parameter %d",
                            args[i], i);
                    return;
                } else {
                    invokeArgs[i] = args[i].equals("true");
                }
            } else {
                throw new CommandException("Invalid parameter type (%s) at parameter %d.",
                        parameterTypes[i].getSimpleName(), i);
            }
        }

        try {
            targetMethod.invoke(targetInstance, invokeArgs);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CommandException("Failed to invoke handler method: %s.", e.getMessage());
        }
    }
}
