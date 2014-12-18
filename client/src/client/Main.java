package client;

import findfour.shared.events.EventHandler;
import findfour.shared.network.Packet;
import findfour.shared.network.PacketTest;
import findfour.shared.network.TcpClient;

public class Main {

    private static TcpClient client = new TcpClient();

    public static void main(String[] args) throws InterruptedException {
        client.registerEventHandlers(new Main());
        client.registerStaticEventHandlers(Main.class);
        client.connect("127.0.0.1", 6666, 1000);

        while (client.isConnected()) {
            Thread.sleep(100);
        }
    }

    @EventHandler(eventId = TcpClient.EVENT_CONNECTED)
    private void connected() {
        System.out.println("Connected");
    }

    @EventHandler(eventId = TcpClient.EVENT_DISCONNECTED)
    private void disconnected() {
        System.out.println("Disconnected");
    }

    @EventHandler(eventId = TcpClient.EVENT_CONNECTED)
    private static void staticTest() {
        System.out.println("Static event");
    }

    @EventHandler(eventId = TcpClient.EVENT_PACKET_RECEIVED)
    private void packetReceived(Packet packet) {
        System.out.println("Received packet");
        PacketTest packetTest = (PacketTest) packet;

        System.out.println("Message: " + packetTest.getData());

        client.send(packetTest);
    }
}
