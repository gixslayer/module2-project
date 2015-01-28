package findfour.shared.network;

import findfour.shared.events.EventRaiser;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class TcpClient extends EventRaiser implements Runnable {
    public static final int EVENT_CONNECTED = 0;
    public static final int EVENT_CONNECT_FAILED = 1;
    public static final int EVENT_DISCONNECTED = 2;
    public static final int EVENT_PACKET_RECEIVED = 3;
    public static final int EVENT_SEND_FAILED = 4;

    private Socket socket;
    private SocketAddress address;
    //private InputStream input;
    private BufferedReader input;
    //private OutputStream output;
    private BufferedWriter output;
    private Thread receiveThread;
    private volatile boolean connected;

    public TcpClient() {
        this.connected = false;

        dispatcher.registerEvent(EVENT_CONNECTED);
        dispatcher.registerEvent(EVENT_CONNECT_FAILED, String.class);
        dispatcher.registerEvent(EVENT_DISCONNECTED);
        dispatcher.registerEvent(EVENT_PACKET_RECEIVED, String.class);
        dispatcher.registerEvent(EVENT_SEND_FAILED, String.class);
    }

    public void connect(String host, int port, int timeout) {
        try {
            address = new InetSocketAddress(host, port);
            socket = new Socket();
            socket.connect(address, timeout);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
            try {
                receiveThread.join();
                receiveThread.notifyAll();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void send(String packet) {

        try {
            output.write(packet);
            output.newLine(); // TODO: Is this needed?
            output.flush();
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
        String buffer;

        while (connected) {
            try {
                buffer = input.readLine();

                if (buffer == null) {
                    disconnect();
                } else {
                    dispatcher.raiseEvent(EVENT_PACKET_RECEIVED, buffer);
                }
            } catch (IOException e) {
                disconnect();
            }
        }
    }

    public Thread getReceiveThread() {
        return receiveThread;
    }
}
