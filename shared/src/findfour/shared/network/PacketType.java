package findfour.shared.network;

public enum PacketType {
    Test(0);

    private final byte value;

    private PacketType(int argValue) {
        this.value = (byte) argValue;
    }

    public byte getValue() {
        return value;
    }

    public static PacketType fromValue(byte argValue) {
        switch (argValue) {
            case 0:
                return Test;

            default:
                return null;
        }
    }
}
