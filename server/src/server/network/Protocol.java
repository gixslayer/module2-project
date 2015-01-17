package server.network;

public abstract class Protocol {
    public abstract void handlePacket(String packet);
}
