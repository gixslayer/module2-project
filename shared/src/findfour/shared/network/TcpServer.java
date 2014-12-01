package findfour.shared.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import findfour.shared.ArgumentException;
import findfour.shared.events.EventDispatcher;
import findfour.shared.events.EventHandler;

public class TcpServer implements Runnable {
    public static final int EVENT_STARTED = 0;
    public static final int EVENT_START_FAILED = 1;
    public static final int EVENT_STOPPED = 2;
    public static final int EVENT_CLIENT_CONNECTED = 3;
    public static final int EVENT_CLIENT_DISCONNECTED = 4;
    public static final int EVENT_PACKET_RECEIVED = 5;

    private final EventDispatcher dispatcher;
    private final Map<Integer, Client> connectedClients;
    private final Object syncRoot;
    private ServerSocket serverSocket;
    private Thread listenThread;
    private int lastClientId;
    private volatile boolean keepListening;

    public TcpServer() {
        this.syncRoot = new Object();
        this.dispatcher = new EventDispatcher();
        this.connectedClients = new HashMap<Integer, Client>();
        this.lastClientId = -1;

        dispatcher.registerEvent(EVENT_STARTED, int.class);
        dispatcher.registerEvent(EVENT_START_FAILED, int.class, String.class);
        dispatcher.registerEvent(EVENT_STOPPED);
        dispatcher.registerEvent(EVENT_CLIENT_CONNECTED, int.class, String.class);
        dispatcher.registerEvent(EVENT_CLIENT_DISCONNECTED, int.class);
        dispatcher.registerEvent(EVENT_PACKET_RECEIVED, int.class, Packet.class);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            listenThread = new Thread(this, "TcpServer-listen");
            keepListening = true;

            // Raise the started event before starting to listen to ensure that the started event
            // is handled before a client connected event might be raised.
            dispatcher.raiseEvent(EVENT_STARTED, port);

            listenThread.start();
        } catch (IOException e) {
            dispatcher.raiseEvent(EVENT_START_FAILED, port, e.getMessage());
        }
    }

    public void stop() {
        keepListening = false;
        try {
            serverSocket.close();

            dispatcher.raiseEvent(EVENT_STOPPED);
        } catch (IOException e) {
            // TODO: Log?
            // Useless statement for now to prevent a Checkstyle warning.
            e.hashCode();
        }
    }

    public void send(int clientId, Packet packet) {
        if (!hasClient(clientId)) {
            throw new ArgumentException("clientId", "Unknown client id: %d", clientId);
        }

        connectedClients.get(clientId).send(packet);
    }

    public boolean hasClient(int clientId) {
        return connectedClients.containsKey(clientId);
    }

    public void registerEventHandlers(Object handlerClass) {
        dispatcher.registerEventHandlers(handlerClass);
    }

    public boolean isRunning() {
        return keepListening;
    }

    @Override
    public void run() {
        while (keepListening) {
            try {
                Socket clientSocket = serverSocket.accept();
                int clientId = getNextClientId();
                Client client;

                try {
                    client = new Client(clientId, clientSocket);

                } catch (IOException e) {
                    // Client socket accepted correctly, but then failed to open input/output
                    // streams. Drop the client.
                    // TODO: Log?
                    continue;
                }

                registerClient(client);

            } catch (IOException e) {
                stop();
            }
        }
    }

    private int getNextClientId() {
        int clientId = lastClientId + 1;

        while (hasClient(clientId)) {
            clientId = clientId == Integer.MAX_VALUE ? 0 : clientId + 1;
        }

        lastClientId = clientId == Integer.MAX_VALUE ? -1 : clientId;

        return clientId;
    }

    private void registerClient(Client client) {
        client.registerHandler(this);
        client.startReceiving();

        synchronized (syncRoot) {
            connectedClients.put(client.getClientId(), client);
        }

        dispatcher
                .raiseEvent(EVENT_CLIENT_CONNECTED, client.getClientId(), client.getHostAddress());
    }

    // --- Client event handlers ---
    @EventHandler(eventId = Client.EVENT_DISCONNECTED)
    private void clientDisconnected(int clientId) {
        synchronized (syncRoot) {
            connectedClients.remove(clientId);
        }

        dispatcher.raiseEvent(EVENT_CLIENT_DISCONNECTED, clientId);
    }

    @EventHandler(eventId = Client.EVENT_PACKET_RECEIVED)
    private void clientPacketReceived(int clientId, Packet packet) {
        dispatcher.raiseEvent(EVENT_PACKET_RECEIVED, clientId, packet);
    }

    private class Client implements Runnable {
        public static final int BUFFER_SIZE = 4096;
        public static final int EVENT_DISCONNECTED = 0;
        public static final int EVENT_PACKET_RECEIVED = 1;

        private final EventDispatcher dispatcher;
        private final int clientId;
        private final Socket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private final byte[] buffer;
        private final PacketBuffer packetBuffer;
        private volatile boolean connected;

        public Client(int argClientId, Socket argSocket) throws IOException {
            this.dispatcher = new EventDispatcher();
            this.clientId = argClientId;
            this.socket = argSocket;
            this.inputStream = argSocket.getInputStream();
            this.outputStream = argSocket.getOutputStream();
            this.buffer = new byte[BUFFER_SIZE];
            this.packetBuffer = new PacketBuffer();
            this.connected = true;

            dispatcher.registerEvent(EVENT_DISCONNECTED, int.class);
            dispatcher.registerEvent(EVENT_PACKET_RECEIVED, int.class, Packet.class);
        }

        public void registerHandler(Object handlerClass) {
            dispatcher.registerEventHandlers(handlerClass);
        }

        public void startReceiving() {
            new Thread(this, String.format("TcpServer-client%d-receive", clientId)).start();
        }

        public int getClientId() {
            return clientId;
        }

        public String getHostAddress() {
            InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();

            return address.getHostString();
        }

        public void send(Packet packet) {
            byte[] data = packet.serialize();

            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.getMessage();
            }
        }

        @Override
        public void run() {
            int bytesReceived;

            while (connected) {
                try {
                    bytesReceived = inputStream.read(buffer);
                } catch (IOException e) {
                    disconnect();
                    break;
                }

                if (bytesReceived < 1) {
                    disconnect();
                } else {
                    packetBuffer.addData(buffer, 0, bytesReceived);

                    while (packetBuffer.hasPacket()) {
                        Packet packet = packetBuffer.nextPacket();
                        dispatcher.raiseEvent(EVENT_PACKET_RECEIVED, clientId, packet,
                                bytesReceived);
                    }
                }
            }
        }

        private void disconnect() {
            try {
                connected = false;
                socket.close();
            } catch (IOException e) {
                // TODO: Log?
                e.printStackTrace();
            }

            dispatcher.raiseEvent(EVENT_DISCONNECTED, clientId);
        }
    }
}
