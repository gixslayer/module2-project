package findfour.shared.network;

public class PacketBufferException extends Exception {
    private static final long serialVersionUID = 1L;

    public PacketBufferException(String message) {
        super(message);
    }

    public PacketBufferException(String format, Object... args) {
        super(String.format(format, args));
    }
}
