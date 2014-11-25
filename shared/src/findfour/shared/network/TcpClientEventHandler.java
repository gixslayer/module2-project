package findfour.shared.network;

public interface TcpClientEventHandler {
    void connected();

    void connectFailed(String reason);

    void disconnected();

    void disconnectFailed(String reason);

    void packetSend(Packet packet, int packetSize);

    void packetSendFailed(Packet packet, String reason);

    void packetReceived(Packet packet, int packetSize);

    void packetReceivedFailed(String reason);
}
