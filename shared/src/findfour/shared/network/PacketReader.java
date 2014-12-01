package findfour.shared.network;

import java.io.UnsupportedEncodingException;

import findfour.shared.ArgumentOutOfRangeException;

class PacketReader {
    private byte[] buffer;
    private int offset;
    private int length;

    public PacketReader(byte[] argBuffer, int argOffset, int argLength) {
        this.buffer = argBuffer;
        this.offset = argOffset;
        this.length = argLength;
    }

    public byte readByte() {
        ensureAvailable(1);

        return buffer[offset++];
    }

    public byte[] readBytes(int count) {
        if (count < 0) {
            throw new ArgumentOutOfRangeException("count", 0, Integer.MAX_VALUE);
        }

        ensureAvailable(count);
        byte[] result = new byte[count];

        System.arraycopy(buffer, offset, result, 0, count);
        offset += count;

        return result;
    }

    public byte[] readByteArray() {
        int arrayLength = readInt();

        return readBytes(arrayLength);
    }

    public boolean readBoolean() {
        return readByte() == 0;
    }

    public int readInt() {
        byte[] intBytes = readBytes(4);
        int result = 0;

        result |= intBytes[0] << 24;
        result |= intBytes[1] << 16;
        result |= intBytes[2] << 8;
        result |= intBytes[3];

        return result;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public String readString() {
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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int newOffset) {
        if (newOffset < 0 || newOffset > buffer.length) {
            throw new ArgumentOutOfRangeException("newOffset", 0, buffer.length + 1);
        }

        offset = newOffset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int newLength) {
        if (newLength < 0 || newLength > buffer.length) {
            throw new ArgumentOutOfRangeException("newLength", 0, buffer.length + 1);
        }

        length = newLength;
    }

    public int remainingDataSize() {
        return length - offset;
    }

    private void ensureAvailable(int bytes) {
        if (offset + bytes > length) {
            throw new PacketReaderException("Unexpected end of data.");
        }
    }
}
