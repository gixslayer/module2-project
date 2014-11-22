package findfour.shared.network;

public class PacketDeserializationException extends Exception {
    private static final long serialVersionUID = 1L;

    public PacketDeserializationException(String message) {
        super(message);
    }
}
