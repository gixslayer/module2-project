package findfour.shared.network;

public abstract class Packet {
    private final PacketType type;

    public Packet(PacketType argType) {
        this.type = argType;
    }

    protected abstract int getDataSize();

    protected abstract void serializeData(PacketWriter writer);

    protected abstract void deserializeData(PacketReader reader);

    public byte[] serialize() {
        PacketWriter writer = new PacketWriter(getDataSize() + 1);

        writer.writeByte(type.getValue());

        serializeData(writer);

        return writer.getBuffer();
    }

    public static Packet deserialize(byte[] buffer) throws PacketDeserializationException {
        PacketReader reader = new PacketReader(buffer, 0);

        try {
            byte packetTypeByte = reader.readByte();
            PacketType packetType = PacketType.fromValue(packetTypeByte);
            Packet packet = Packet.fromType(packetType);

            packet.deserializeData(reader);

            return packet;
        } catch (PacketReaderException e) {
            throw new PacketDeserializationException(e.getMessage());
        }
    }

    public static Packet fromType(PacketType type) {
        return null;
    }
}
