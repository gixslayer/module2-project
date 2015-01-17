package findfour.shared.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import findfour.shared.events.EventHandler;
import findfour.shared.events.EventRaiser;

public class TcpServer extends EventRaiser implements Runnable {
    public static final int EVENT_STARTED = 0;
    public static final int EVENT_START_FAILED = 1;
    public static final int EVENT_STOPPED = 2;
    public static final int EVENT_CLIENT_CONNECTED = 3;
    public static final int EVENT_CLIENT_DISCONNECTED = 4;
    public static final int EVENT_PACKET_RECEIVED = 5;
    public static final int EVENT_SEND_FAILED = 6;

    // TODO: Do we need to maintain a list of connected clients?
    // It can be used to broadcast a message to all clients, but will we use that directly through
    // this class? If this class doesn't store a reference, nor does an external class how will
    // Java handle garbage collection, will having a running thread prevent the garbage collection
    // or will it still be marked as unreachable?
    private final List<Client> connectedClients;
    private final Object syncRoot;
    private ServerSocket serverSocket;
    private Thread listenThread;
    private volatile boolean keepListening;

    public TcpServer() {
        this.syncRoot = new Object();
        this.connectedClients = new LinkedList<Client>();

        dispatcher.registerEvent(EVENT_STARTED, int.class);
        dispatcher.registerEvent(EVENT_START_FAILED, int.class, String.class);
        dispatcher.registerEvent(EVENT_STOPPED);
        dispatcher.registerEvent(EVENT_CLIENT_CONNECTED, Client.class, String.class);
        dispatcher.registerEvent(EVENT_CLIENT_DISCONNECTED, Client.class);
        dispatcher.registerEvent(EVENT_PACKET_RECEIVED, Client.class, String.class);
        dispatcher.registerEvent(EVENT_SEND_FAILED, Client.class, String.class);
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

    public synchronized void stop() {
        // Don't bother stopping if the server isn't running.
        if (!keepListening) {
            return;
        }

        keepListening = false;

        /* 
         * Disconnect all clients when the server stops. Calling disconnect will raise the
         * EVENT_CLIENT_DISCONNECTED, in which the handler will try to obtain a lock to syncRoot
         * which is already obtained and then remove the client from the connectedClients list.
         * This is why the connectedClients is copied to a local array (to prevent modifying the 
         * connectedClients collection while iterating).  
         */
        synchronized (syncRoot) {
            Client[] clients = connectedClients.toArray(new Client[0]);

            for (Client client : clients) {
                client.disconnect();
            }
        }

        try {
            serverSocket.close();

            dispatcher.raiseEvent(EVENT_STOPPED);
        } catch (IOException e) {
            // TODO: Log?
            // Useless statement for now to prevent a Checkstyle warning.
            e.hashCode();
        }
    }

    public boolean isRunning() {
        return keepListening;
    }

    @Override
    public void run() {
        while (keepListening) {
            try {
                Socket clientSocket = serverSocket.accept();

                try {
                    Client client = new Client(clientSocket);
                    registerClient(client);
                } catch (IOException e) {
                    // Client socket accepted correctly, but then failed to open input/output
                    // streams. Drop the client.
                    clientSocket.close();
                }

            } catch (IOException e) {
                stop();
            }
        }
    }

    private void registerClient(Client client) {
        client.registerEventHandlers(this);
        client.startReceiving();

        synchronized (syncRoot) {
            connectedClients.add(client);
        }

        dispatcher.raiseEvent(EVENT_CLIENT_CONNECTED, client, client.getHostAddress());
    }

    // --- Client event handlers ---
    @EventHandler(eventId = Client.EVENT_DISCONNECTED)
    private void clientDisconnected(Client client) {
        synchronized (syncRoot) {
            connectedClients.remove(client);
        }

        dispatcher.raiseEvent(EVENT_CLIENT_DISCONNECTED, client);
    }

    @EventHandler(eventId = Client.EVENT_PACKET_RECEIVED)
    private void clientPacketReceived(Client client, String packet) {
        dispatcher.raiseEvent(EVENT_PACKET_RECEIVED, client, packet);
    }

    @EventHandler(eventId = Client.EVENT_SEND_FAILED)
    private void clientSendFailed(Client client, String reason) {
        dispatcher.raiseEvent(EVENT_SEND_FAILED, client, reason);
    }

    public class Client extends EventRaiser implements Runnable {
        public static final int EVENT_DISCONNECTED = 0;
        public static final int EVENT_PACKET_RECEIVED = 1;
        public static final int EVENT_SEND_FAILED = 2;

        private final Socket socket;
        private final BufferedReader input;
        private final BufferedWriter output;
        private volatile boolean connected;

        public Client(Socket argSocket) throws IOException {
            this.socket = argSocket;
            this.input = new BufferedReader(new InputStreamReader(argSocket.getInputStream()));
            this.output = new BufferedWriter(new OutputStreamWriter(argSocket.getOutputStream()));
            this.connected = true;

            dispatcher.registerEvent(EVENT_DISCONNECTED, Client.class);
            dispatcher.registerEvent(EVENT_PACKET_RECEIVED, Client.class, String.class);
            dispatcher.registerEvent(EVENT_SEND_FAILED, Client.class, String.class);
        }

        public void startReceiving() {
            new Thread(this, String.format("TcpServer-client%d-receive", this.hashCode())).start();
        }

        public String getHostAddress() {
            InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();

            return address.getHostString();
        }

        public void send(String packet) {
            try {
                output.write(packet);
                output.newLine();
                output.flush();
            } catch (IOException e) {
                dispatcher.raiseEvent(EVENT_SEND_FAILED, this, e.getMessage());

                // Drop the connection if data cannot be send for whatever reason.
                disconnect();
            }
        }

        @Override
        public void run() {
            String buffer;

            while (connected) {
                try {
                    buffer = input.readLine();

                    if (buffer == null) {
                        disconnect();
                    } else {
                        dispatcher.raiseEvent(EVENT_PACKET_RECEIVED, this, buffer);
                    }
                } catch (IOException e) {
                    disconnect();
                }
            }
        }

        public void disconnect() {
            try {
                socket.close();
            } catch (IOException e) {
                // There is no sensible thing to do when the socket, for whatever reason, fails to
                // close. Do something (even though it's useless) to prevent a Checkstyle warning.
                e.getMessage();
            } finally {
                connected = false;

                dispatcher.raiseEvent(EVENT_DISCONNECTED, this);
            }
        }
    }
}
