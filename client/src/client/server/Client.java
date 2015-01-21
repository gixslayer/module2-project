package client.server;

import client.network.Protocol;
import findfour.shared.events.EventHandler;
import findfour.shared.game.Board;
import findfour.shared.game.Disc;
import findfour.shared.network.TcpClient;

/**
 * Created by joran on 21-1-15.
 */
public class Client extends Thread {
    public static final String INITIAL_NAME = "name";
    public static final String INITIAL_GROUP = "19";
    public static final String[] INITIAL_EXTENSIONS = new String[0];
    private String servername = "127.0.0.1";
    private int serverport = 6666;
    private String name;
    private String group;
    private String[] supportedExtensions;

    private boolean ready = false;
    private String opponent;
    private boolean myTurn = false;
    private Disc disc;

    private final TcpClient tcpclient;
    private final Protocol protocol;
    private Board board;

    // ---------------------------------------Constructor -----------------------------------------
    public Client(TcpClient argClient, String argName, String argGroup,
            String[] argSupportedExtensions) {
        this.tcpclient = argClient;
        this.name = argName;
        this.group = argGroup;
        this.supportedExtensions = argSupportedExtensions;
        protocol = new Protocol(tcpclient, this);
    }

    public Client() {
        this.tcpclient = new TcpClient();
        this.name = INITIAL_NAME;
        this.group = INITIAL_GROUP;
        this.supportedExtensions = INITIAL_EXTENSIONS;
        protocol = new Protocol(tcpclient, this);
    }

    //---------------------------------------Methods-----------------------------------------------

    public TcpClient getTcpclient() {
        return tcpclient;
    }

    public String getClientName() {
        return name;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void doMove(int i, String player) {
        if (player.equals(name)) {
            board.makeMove(i, Disc.Red);
        } else if (player.equals(opponent)) {
            board.makeMove(i, Disc.Yellow);
        } else {
            System.out.println("Invalid playername");
        }

    }

    public void setMyTurn(boolean argMyTurn) {
        this.myTurn = argMyTurn;
    }

    public void resetBoard() {
        this.board = new Board();
    }

    public String getGroup() {
        return group;
    }

    public void setOpponent(String argOpponent) {
        this.opponent = argOpponent;
    }

    public String getOpponent() {
        return opponent;
    }

    public boolean hasOpponent() {
        return opponent != null;
    }

    public boolean isRunning() {
        return tcpclient.isConnected();
    }

    public void stopConnection() {
        tcpclient.disconnect();
    }

    public void setReady(Boolean b) {
        this.ready = b;
    }

    public void tellReady() {
        if (ready) {
            protocol.sendReady();
        } else {
            System.out.println("Not accepted by server");
        }
    }

    public int getServerport() {
        return serverport;
    }

    public void setServerport(int argServerport) {
        this.serverport = argServerport;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String argServername) {
        this.servername = argServername;
    }

    @Override
    public void run() {
        if (!tcpclient.isConnected()) {
            this.connect();
        }
    }

    public void connect() {
        //Set up connection
        tcpclient.registerEventHandlers(this);
        tcpclient.registerStaticEventHandlers(Client.class);
        tcpclient.connect(servername, serverport, 1000);
        protocol.sendJoin(name, group);

        while (tcpclient.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
