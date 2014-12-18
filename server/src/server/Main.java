package server;

import findfour.shared.events.EventHandler;
import findfour.shared.network.Packet;
import findfour.shared.network.PacketTest;
import findfour.shared.network.TcpServer;

public class Main {
    private static TcpServer server = new TcpServer();

    public static void main(String[] args) throws InterruptedException {
        server.registerEventHandlers(new Main());
        server.start(6666);

        while (server.isRunning()) {
            Thread.sleep(100);
        }
    }

    @EventHandler(eventId = TcpServer.EVENT_STARTED)
    private void started(int port) {
        System.out.printf("Started on port %d\n", port);
    }

    @EventHandler(eventId = TcpServer.EVENT_STOPPED)
    private void stopped() {
        System.out.println("Stopped");
    }

    @EventHandler(eventId = TcpServer.EVENT_CLIENT_CONNECTED)
    private void clientConnected(int clientId, String host) {
        System.out.printf("Client connected (%d) from %s\n", clientId, host);

        PacketTest packet = new PacketTest();
        packet.setData(String.format("Hello, %d", clientId));

        server.send(clientId, packet);
    }

    @EventHandler(eventId = TcpServer.EVENT_CLIENT_DISCONNECTED)
    private void clientDisconnected(int clientId) {
        System.out.printf("Client %d disconnected\n", clientId);
    }

    @EventHandler(eventId = TcpServer.EVENT_PACKET_RECEIVED)
    private void packetReceived(int clientId, Packet packet) {
        System.out.printf("Packet from %d\n", clientId);

        PacketTest packetTest = (PacketTest) packet;

        System.out.println("Message: " + packetTest.getData());
    }
}
