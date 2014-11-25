package findfour.shared.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer implements Runnable {
    private final TcpServerEventHandler handler;
    private ServerSocket serverSocket;
    private Thread listenThread;
    private volatile boolean keepListening;

    public TcpServer(TcpServerEventHandler argHandler) {
        this.handler = argHandler;
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            listenThread = new Thread(this);
            keepListening = true;

            listenThread.start();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop() {
        keepListening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (keepListening) {
            try {
                Socket client = serverSocket.accept();
                InetSocketAddress address = (InetSocketAddress) client.getRemoteSocketAddress();
                String hostAddress = address.getHostString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
