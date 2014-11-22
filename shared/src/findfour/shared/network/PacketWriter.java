package findfour.shared.network;

import java.io.UnsupportedEncodingException;

import findfour.shared.ArgumentNullException;
import findfour.shared.ArgumentOutOfRangeException;

public class PacketWriter {
    private byte[] buffer;
    private int offset;

    public PacketWriter(byte[] argBuffer, int argOffset) {
        if (argBuffer == null) {
            throw new ArgumentNullException("argBuffer");
        } else if (argOffset < 0 || argOffset > argBuffer.length) {
            throw new ArgumentOutOfRangeException("argOffset", 0, argBuffer.length + 1);
        }

        this.buffer = argBuffer;
        this.offset = argOffset;
    }

    public PacketWriter(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new ArgumentOutOfRangeException("initialCapacity", 0, Integer.MAX_VALUE);
        }

        this.buffer = new byte[initialCapacity];
        this.offset = 0;
    }

    // Write primitive methods.

    public void writeByte(byte value) {
        ensureAvailable(1);

        buffer[offset++] = value;
    }

    public void writeBytes(byte[] value, int argOffset, int length) {
        if (value == null) {
            throw new ArgumentNullException("value");
        } else if (argOffset < 0 || argOffset > value.length) {
            throw new ArgumentOutOfRangeException("argOffset", 0, value.length + 1);
        } else if (length < 0 || argOffset + length > value.length) {
            throw new ArgumentOutOfRangeException("length", 0, value.length - argOffset + 1);
        }

        ensureAvailable(value.length);

        System.arraycopy(value, argOffset, buffer, offset, length);
        offset += length;
    }

    public void writeByteArray(byte[] value) {
        if (value == null) {
            throw new ArgumentNullException("value");
        }

        ensureAvailable(4 + value.length);

        writeInt(value.length);
        writeBytes(value, 0, value.length);
    }

    public void writeBoolean(boolean value) {
        ensureAvailable(1);

        buffer[offset++] = value ? (byte) 0 : 1;
    }

    public void writeInt(int value) {
        ensureAvailable(4);

        buffer[offset++] = (byte) (value & 0xff);
        buffer[offset++] = (byte) ((value >> 8) & 0xff);
        buffer[offset++] = (byte) ((value >> 16) & 0xff);
        buffer[offset++] = (byte) ((value >> 24) & 0xff);
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeString(String value) {
        if (value == null) {
            throw new ArgumentNullException("value");
        }

        try {
            byte[] utfBytes = value.getBytes("UTF-8");

            writeByteArray(utfBytes);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 must be supported by every implementation of the Java platform. If this
            // exception is thrown something went very wrong.
            assert false : "UTF-8 encoding not supported";
        }
    }

    // Generic getters/setters.

    public void setOffset(int newOffset) {
        if (newOffset < 0 || newOffset > buffer.length) {
            throw new ArgumentOutOfRangeException("newOffset", 0, buffer.length + 1);
        }

        offset = newOffset;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    // Private helper methods.

    private void ensureAvailable(int bytes) {
        if (offset + bytes > buffer.length) {
            // The current buffer is too small, allocate a new buffer which has the requested 
            // capacity and copy the content of the old buffer into the new buffer before updating
            // the reference of the current buffer so that it points to the newly allocated buffer.
            byte[] newBuffer = new byte[offset + bytes];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }
    }
}
