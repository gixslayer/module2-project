package server.player;

public final class NoSuchPlayerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoSuchPlayerException(String message) {
        super(message);
    }
}
