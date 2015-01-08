package findfour.shared.network;

import java.util.LinkedList;
import java.util.Queue;

class PacketBuffer {
    public static final int BUFFER_SIZE = 4096;

    private final Queue<Packet> packets;
    private final byte[] lengthBytes;
    private final byte[] lengthBuffer;
    private int lengthBufferSize;
    private int bytesToReceive;
    private byte[] currentPacket;
    private int currentPacketOffset;

    public PacketBuffer() {
        this.packets = new LinkedList<Packet>();
        this.lengthBytes = new byte[Packet.LENGTH_SIZE];
        this.lengthBuffer = new byte[Packet.LENGTH_SIZE];
        this.lengthBufferSize = 0;
        this.bytesToReceive = 0;
        this.currentPacket = null;
        this.currentPacketOffset = 0;
    }

    public void handleData(byte[] buffer, int offset, int length) throws PacketBufferException {
        assert buffer != null;
        assert offset >= 0;
        assert length > 0;
        assert offset + length <= buffer.length;

        int localOffset = offset;

        while (localOffset < length) {
            if (bytesToReceive == 0) {
                localOffset += startNewPacket(buffer, localOffset, length);
            } else {
                localOffset += continuePacket(buffer, localOffset, length);
            }
        }
    }

    private int startNewPacket(byte[] buffer, int offset, int length) throws PacketBufferException {
        if (length + lengthBufferSize < Packet.LENGTH_SIZE) {
            System.arraycopy(buffer, offset, lengthBuffer, lengthBufferSize, length);
            lengthBufferSize += length;

            return length;
        } else {
            int lengthBytesToCopy = Packet.LENGTH_SIZE - lengthBufferSize;

            System.arraycopy(lengthBuffer, 0, lengthBytes, 0, lengthBufferSize);
            System.arraycopy(buffer, offset, lengthBytes, lengthBufferSize, lengthBytesToCopy);

            bytesToReceive = getPacketLength();

            if (bytesToReceive < Packet.MIN_LENGTH || bytesToReceive > Packet.MAX_LENGTH) {
                throw new PacketBufferException("Invalid packet length: %d.", bytesToReceive);
            }

            currentPacket = new byte[bytesToReceive];
            currentPacketOffset = 0;
            lengthBufferSize = 0;

            return lengthBytesToCopy;
        }
    }

    private int continuePacket(byte[] buffer, int offset, int length) throws PacketBufferException {
        int actualLength = length > bytesToReceive ? bytesToReceive : length;

        System.arraycopy(buffer, offset, currentPacket, currentPacketOffset, actualLength);
        bytesToReceive -= actualLength;

        if (bytesToReceive == 0) {
            completePacket();
        }

        return actualLength;
    }

    private void completePacket() throws PacketBufferException {
        try {
            Packet packet = Packet.deserialize(currentPacket);

            packets.add(packet);
        } catch (PacketDeserializationException e) {
            throw new PacketBufferException("Failed to deserialize packet: %s.", e.getMessage());
        } finally {
            currentPacket = null;
            currentPacketOffset = 0;
        }
    }

    private int getPacketLength() {
        return lengthBytes[0] << 24 | lengthBytes[1] << 16 | lengthBytes[2] << 8 | lengthBytes[3];
    }

    public boolean hasPacket() {
        return !packets.isEmpty();
    }

    public Packet nextPacket() {
        return packets.remove();
    }
}
