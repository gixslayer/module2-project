package findfour.shared.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import findfour.shared.events.EventRaiser;

public class TcpClient extends EventRaiser implements Runnable {
    public static final int BUFFER_SIZE = 4096;
    public static final int EVENT_CONNECTED = 0;
    public static final int EVENT_CONNECT_FAILED = 1;
    public static final int EVENT_DISCONNECTED = 2;
    public static final int EVENT_PACKET_RECEIVED = 3;
    public static final int EVENT_RECEIVE_FAILED = 4;
    public static final int EVENT_SEND_FAILED = 5;

    private final byte[] buffer;
    private final PacketBuffer packetBuffer;
    private Socket socket;
    private SocketAddress address;
    private InputStream input;
    private OutputStream output;
    private Thread receiveThread;
    private volatile boolean connected;

    public TcpClient() {
        this.buffer = new byte[BUFFER_SIZE];
        this.packetBuffer = new PacketBuffer();
        this.connected = false;

        dispatcher.registerEvent(EVENT_CONNECTED);
        dispatcher.registerEvent(EVENT_CONNECT_FAILED, String.class);
        dispatcher.registerEvent(EVENT_DISCONNECTED);
        dispatcher.registerEvent(EVENT_PACKET_RECEIVED, Packet.class);
        dispatcher.registerEvent(EVENT_RECEIVE_FAILED, String.class);
        dispatcher.registerEvent(EVENT_SEND_FAILED, String.class);
    }

    public void connect(String host, int port, int timeout) {
        try {
            address = new InetSocketAddress(host, port);
            socket = new Socket();
            socket.connect(address, timeout);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            connected = true;
            receiveThread = new Thread(this, "TcpClient-receive");

            // Raise the connected event before starting to receive to ensure that the connected
            // event is handled before a packet received event might be raised.
            dispatcher.raiseEvent(EVENT_CONNECTED);

            receiveThread.start();
        } catch (SocketTimeoutException e) {
            dispatcher.raiseEvent(EVENT_CONNECT_FAILED, "Connection attempt timed out.");
        } catch (IOException e) {
            dispatcher.raiseEvent(EVENT_CONNECT_FAILED, e.getMessage());
        }
    }

    public void disconnect() {
        try {
            // Closing the socket will also close the underlying input/output streams.
            socket.close();
        } catch (IOException e) {
            // There is no sensible thing to do when the socket, for whatever reason, fails to
            // close. Do something (even though it's useless) to prevent a Checkstyle warning.
            e.getMessage();
        } finally {
            input = null;
            output = null;
            connected = false;

            dispatcher.raiseEvent(EVENT_DISCONNECTED);
        }
    }

    public void send(Packet packet) {
        byte[] data = packet.serialize();

        try {
            output.write(data);
        } catch (IOException e) {
            dispatcher.raiseEvent(EVENT_SEND_FAILED, e.getMessage());

            // Drop the connection if data cannot be send for whatever reason.
            disconnect();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        int bytesReceived;

        while (connected) {
            try {
                bytesReceived = input.read(buffer);
            } catch (IOException e) {
                disconnect();
                break;
            }

            if (bytesReceived < 1) {
                disconnect();
            } else {
                try {
                    packetBuffer.handleData(buffer, 0, bytesReceived);

                    while (packetBuffer.hasPacket()) {
                        Packet packet = packetBuffer.nextPacket();
                        dispatcher.raiseEvent(EVENT_PACKET_RECEIVED, packet);
                    }
                } catch (PacketBufferException e) {
                    dispatcher.raiseEvent(EVENT_RECEIVE_FAILED, e.getMessage());

                    // When a packet could not be deserialized properly it is unlikely following
                    // packets will be properly deserialized so drop the connection.
                    disconnect();
                }
            }
        }
    }
}
