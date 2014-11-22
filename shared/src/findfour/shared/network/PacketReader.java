package findfour.shared.network;

import java.io.UnsupportedEncodingException;

import findfour.shared.ArgumentOutOfRangeException;

public class PacketReader {
    private byte[] buffer;
    private int offset;

    public PacketReader(byte[] argBuffer, int argOffset) {
        this.buffer = argBuffer;
        this.offset = argOffset;
    }

    public byte readByte() throws PacketReaderException {
        ensureAvailable(1);

        return buffer[offset++];
    }

    public byte[] readBytes(int count) throws PacketReaderException {
        if (count < 0) {
            throw new ArgumentOutOfRangeException("count", 0, Integer.MAX_VALUE);
        }

        ensureAvailable(count);
        byte[] result = new byte[count];

        System.arraycopy(buffer, offset, result, 0, count);
        offset += count;

        return result;
    }

    public byte[] readByteArray() throws PacketReaderException {
        int length = readInt();

        return readBytes(length);
    }

    public boolean readBoolean() throws PacketReaderException {
        return readByte() == 0;
    }

    public int readInt() throws PacketReaderException {
        byte[] intBytes = readBytes(4);
        int result = 0;

        result |= intBytes[0] >> 24;
        result |= intBytes[1] >> 16;
        result |= intBytes[2] >> 8;
        result |= intBytes[3];

        return result;
    }

    public float readFloat() throws PacketReaderException {
        return Float.intBitsToFloat(readInt());
    }

    public String readString() throws PacketReaderException {
        byte[] utfBytes = readByteArray();

        try {
            return new String(utfBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // UTF-8 must be supported by every implementation of the Java platform. If this
            // exception is thrown something went very wrong.
            assert false : "UTF-8 encoding not supported";
            return null;
        }
    }

    private void ensureAvailable(int bytes) throws PacketReaderException {
        if (offset + bytes > buffer.length) {
            throw new PacketReaderException("Unexpected end of data.");
        }
    }
}
