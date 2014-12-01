package findfour.shared.network;

import java.util.LinkedList;
import java.util.Queue;

class PacketBuffer {
    public static final int BUFFER_SIZE = 4096;

    private final Queue<Packet> packets;
    private final byte[] buffer;
    private final PacketReader reader;
    private int currentOffset;

    public PacketBuffer() {
        this.packets = new LinkedList<Packet>();
        this.buffer = new byte[BUFFER_SIZE];
        this.reader = new PacketReader(buffer, 0, 0);
        this.currentOffset = 0;
    }

    public void addData(byte[] argBuffer, int offset, int length) {
        assert argBuffer != null;
        assert offset >= 0;
        assert length > 0;
        assert offset + length <= argBuffer.length;

        if (currentOffset + length > BUFFER_SIZE) {
            // Receive buffer overflow, figure out an elegant solution.
            return;
        }

        System.arraycopy(argBuffer, offset, buffer, currentOffset, length);
        currentOffset += length;

        deserializePackets();
    }

    private void deserializePackets() {
        reader.setOffset(0);
        reader.setLength(currentOffset);

        while (reader.remainingDataSize() >= 5) {
            byte packetType = reader.readByte();
            int contentLength = reader.readInt();

            if (reader.remainingDataSize() >= contentLength) {
                byte[] content = reader.readBytes(contentLength);

                try {
                    Packet packet = Packet.deserialize(packetType, content);

                    packets.add(packet);
                } catch (PacketDeserializationException e) {
                    // Todo: Log?
                    e.getMessage();
                }
            } else {
                reader.setOffset(reader.getOffset() - 5);
                break;
            }
        }

        int bytesProcessed = reader.getOffset();
        int bytesUnprocessed = currentOffset - bytesProcessed;

        System.arraycopy(buffer, bytesProcessed, buffer, 0, bytesUnprocessed);
        currentOffset = bytesUnprocessed;
    }

    public boolean hasPacket() {
        return !packets.isEmpty();
    }

    public Packet nextPacket() {
        return packets.remove();
    }
}
