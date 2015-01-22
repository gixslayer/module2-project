package client.network;

import client.ClientController;
import findfour.shared.events.EventHandler;
import findfour.shared.network.TcpClient;

/**
 * Created by joran on 21-1-15.
 */
public class Connection extends Thread {
    private final TcpClient tcpclient;
    private ClientController clientController;
    private Protocol protocol;
    private String servername = "127.0.0.1";
    private int serverport = 6666;

    public Connection(ClientController client) {
        this.clientController = client;
        tcpclient = new TcpClient();
        protocol = new Protocol(tcpclient, clientController);
    }

    //-----------------Methods
    @Override
    public void run() {
        if (!tcpclient.isConnected()) {
            this.connect();
        }
    }

    //This function sets up an connection with the server
    public void connect() {
        //Set up connection
        tcpclient.registerEventHandlers(this);
        tcpclient.registerStaticEventHandlers(ClientController.class);
        tcpclient.connect(servername, serverport, 1000);
        protocol.sendJoin(clientController.getClientName(), clientController.getGroup());
        while (tcpclient.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //-----------------Getters and Setters---------------------------------
    public ClientController getClient() {
        return clientController;
    }

    public TcpClient getTcpclient() {
        return tcpclient;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol argProtocol) {
        this.protocol = argProtocol;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String argServername) {
        this.servername = argServername;
    }

    public int getServerport() {
        return serverport;
    }

    public void setServerport(int argServerport) {
        this.serverport = argServerport;
    }

    //-------------------------------Eventhandling for connections---------------------------------
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
    private void packetReceived(String packet) {
        System.out.println("Received packet");
        System.out.println("Message: " + packet);
    }
}