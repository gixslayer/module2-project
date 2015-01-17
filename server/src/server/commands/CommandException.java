package server.commands;

public final class CommandException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String format, Object... args) {
        super(String.format(format, args));
    }
}
