package findfour.shared.network;

public abstract class Packet {
    private final PacketType type;

    public Packet(PacketType argType) {
        this.type = argType;
    }

    protected abstract int getContentSize();

    protected abstract void serializeContent(PacketWriter writer);

    protected abstract void deserializeContent(PacketReader reader);

    byte[] serialize() {
        int contentSize = getContentSize();
        PacketWriter writer = new PacketWriter(contentSize + 5);

        writer.writeByte(type.getValue());
        writer.writeInt(contentSize);

        serializeContent(writer);

        return writer.getBuffer();
    }

    public PacketType getType() {
        return type;
    }

    static Packet deserialize(byte packetTypeByte, byte[] contentBuffer)
            throws PacketDeserializationException {

        try {
            PacketType packetType = PacketType.fromValue(packetTypeByte);
            Packet packet = Packet.fromType(packetType);
            PacketReader reader = new PacketReader(contentBuffer, 0, contentBuffer.length);

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
