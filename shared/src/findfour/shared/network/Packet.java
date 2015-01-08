package findfour.shared.network;

public abstract class Packet {
    public static final int TYPE_SIZE = 1;
    public static final int LENGTH_SIZE = 4;
    public static final int MAX_LENGTH = 65536;
    public static final int MIN_LENGTH = TYPE_SIZE;

    private final PacketType type;

    public Packet(PacketType argType) {
        this.type = argType;
    }

    protected abstract int getContentSize();

    protected abstract void serializeContent(PacketWriter writer);

    protected abstract void deserializeContent(PacketReader reader);

    byte[] serialize() {
        int contentSize = getContentSize();

        assert contentSize + TYPE_SIZE < MAX_LENGTH;

        PacketWriter writer = new PacketWriter(contentSize + LENGTH_SIZE + TYPE_SIZE);

        writer.writeInt(contentSize + TYPE_SIZE);
        writer.writeByte(type.getValue());

        serializeContent(writer);

        return writer.getBuffer();
    }

    public PacketType getType() {
        return type;
    }

    static Packet deserialize(byte[] packetData) throws PacketDeserializationException {

        try {
            PacketType packetType = PacketType.fromValue(packetData[0]);
            Packet packet = Packet.fromType(packetType);
            PacketReader reader = new PacketReader(packetData, TYPE_SIZE, packetData.length);

            packet.deserializeContent(reader);

            return packet;
        } catch (PacketReaderException e) {
            throw new PacketDeserializationException(e.getMessage());
        }
    }

    static Packet fromType(PacketType type) {
        switch (type) {
            case Test:
                return new PacketTest();

            default:
                return null;
        }
    }
}
