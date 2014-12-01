package findfour.shared.network;

class PacketDeserializationException extends Exception {
    private static final long serialVersionUID = 1L;

    public PacketDeserializationException(String message) {
        super(message);
    }
}
