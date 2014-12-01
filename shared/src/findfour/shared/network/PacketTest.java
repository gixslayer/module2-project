package findfour.shared.network;

public class PacketTest extends Packet {
    private String data;

    public PacketTest() {
        super(PacketType.Test);
    }

    @Override
    protected int getContentSize() {
        return data.length() + 4;
    }

    @Override
    protected void serializeContent(PacketWriter writer) {
        writer.writeString(data);
    }

    @Override
    protected void deserializeContent(PacketReader reader) throws PacketReaderException {
        data = reader.readString();
    }

    public String getData() {
        return data;
    }

    public void setData(String value) {
        data = value;
    }

}
