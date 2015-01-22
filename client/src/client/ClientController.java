package client;

import client.GUI.GuiController;
import client.network.Connection;
import findfour.shared.game.Board;
import findfour.shared.game.Disc;

/**
 * Created by joran on 21-1-15.
 */
public class ClientController extends Thread {
    //-------------------------------Fields--------------------------------------------------------
    public static final String INITIAL_NAME = "name";
    public static final String INITIAL_GROUP = "19";
    public static final String[] INITIAL_EXTENSIONS = new String[0];
    public GuiController guiController = new GuiController(this);
    private String clientName;
    private String group;
    private String[] supportedExtensions;
    private Connection connection = new Connection(this);
    private boolean ready = false;
    private String opponent;
    private boolean myTurn = false;
    private Disc disc;
    private Board board;

    // ---------------------------------------Constructor -----------------------------------------
    public ClientController(String argName, String argGroup, String[] argSupportedExtensions) {
        this.clientName = argName;
        this.group = argGroup;
        this.supportedExtensions = argSupportedExtensions;
    }

    public ClientController() {
        this.clientName = INITIAL_NAME;
        this.group = INITIAL_GROUP;
        this.supportedExtensions = INITIAL_EXTENSIONS;
    }

    //---------------------------------------Methods-----------------------------------------------
    @Override
    public void run() {
        this.resetBoard();
        guiController.start();
    }

    public void doMove(int i, String player) {
        if (player.equals(clientName)) {
            board.makeMove(i, Disc.Red);
        } else if (player.equals(opponent)) {
            board.makeMove(i, Disc.Yellow);
        } else {
            System.out.println("Invalid playername");
        }
    }

    public void tellReady() {
        if (ready) {
            connection.getProtocol().sendReady();
        } else {
            System.out.println("Not accepted by server");
        }
    }

    //-------------------------------Small functions-----------------------------------------------
    public boolean isMyTurn() {
        return myTurn;
    }

    public boolean hasOpponent() {
        return opponent != null;
    }

    public void resetBoard() {
        this.board = new Board();
    }

    public boolean isRunning() {
        return connection.getTcpclient().isConnected();
    }

    public void stopConnection() {
        connection.getTcpclient().disconnect();
    }

    //------------------GettersAndSetters--------------------------------------------
    public Connection getConnection() {
        return connection;
    }

    public void setMyTurn(boolean argMyTurn) {
        this.myTurn = argMyTurn;
    }

    public String getGroup() {
        return group;
    }

    public Board getBoard() {
        return board;
    }

    public void setOpponent(String argOpponent) {
        this.opponent = argOpponent;
    }

    public String getOpponent() {
        return opponent;
    }

    public String getClientName() {
        return clientName;
    }

    public void setReady(Boolean b) {
        this.ready = b;
    }
}