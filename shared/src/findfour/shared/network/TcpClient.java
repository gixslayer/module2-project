package findfour.shared.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class TcpClient implements Runnable {
    public static final int BUFFER_SIZE = 4096;

    private final SocketAddress address;
    private final TcpClientEventHandler handler;
    private final byte[] buffer;
    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Thread receiveThread;
    private volatile boolean connected;

    public TcpClient(String host, int port, TcpClientEventHandler argHandler) {
        this.address = new InetSocketAddress(host, port);
        this.handler = argHandler;
        this.buffer = new byte[BUFFER_SIZE];
        this.connected = false;
    }

    public void connect(int timeout) {
        try {
            socket = new Socket();
            socket.connect(address, timeout);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            connected = true;
            receiveThread = new Thread(this);
            receiveThread.start();

            handler.connected();
        } catch (SocketTimeoutException e) {
            handler.connectFailed("Connection attempt timed out.");
        } catch (IOException e) {
            handler.connectFailed(e.getMessage());
        }
    }

    public void disconnect() {
        try {
            // Closing the socket will also close the underlying input/output streams.
            socket.close();
            input = null;
            output = null;
            connected = false;

            handler.disconnected();
        } catch (IOException e) {
            handler.disconnectFailed(e.getMessage());
        }
    }

    public void send(Packet packet) {
        byte[] data = packet.serialize();

        try {
            output.write(data);

            handler.packetSend(packet, data.length);
        } catch (IOException e) {
            handler.packetSendFailed(packet, e.getMessage());
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                int bytesReceived = input.read(buffer);

                if (bytesReceived == 0) {
                    disconnect();
                } else {
                    Packet packet = Packet.deserialize(buffer);

                    handler.packetReceived(packet, bytesReceived);
                }
            } catch (IOException e) {
                disconnect();
            } catch (PacketDeserializationException e) {
                handler.packetReceivedFailed(e.getMessage());
            }
        }
    }
}
