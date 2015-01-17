package server.commands;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;

public class CommandInvoker {
    private final Map<String, CommandReference> handlers;

    public CommandInvoker() {
        this.handlers = new HashMap<String, CommandReference>();
    }

    public void registerHandlers(Object instance) {
        Class<?> handlerClass = instance.getClass();

        for (Method method : handlerClass.getDeclaredMethods()) {
            if (!isHandlerMethodValid(method)) {
                continue;
            }

            String name = extractCommandName(method);
            CommandReference reference = new CommandReference(instance, method);
            handlers.put(name, reference);
        }
    }

    public void invoke(String name, String[] args) {
        if (handlers.containsKey(name)) {
            CommandReference reference = handlers.get(name);

            reference.invoke(args);
        } else {
            Log.error(LogLevel.Minimal, "unknown command: %s", name);
        }
    }

    private boolean isHandlerMethodValid(Method method) {
        if (!method.isAnnotationPresent(CommandHandler.class)) {
            return false;
        } else if (!method.getName().startsWith("cmd")) {
            return false;
        } else if (method.getName().length() < 4) {
            return false;
        }

        return true;
    }

    private String extractCommandName(Method method) {
        String suffix = method.getName().substring(4);
        char firstChar = method.getName().charAt(3);

        return Character.toLowerCase(firstChar) + suffix;
    }
}
