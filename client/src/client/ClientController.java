package client;

import client.GUI.GuiController;
import client.network.Connection;
import findfour.shared.game.Board;
import findfour.shared.game.Disc;

import java.util.HashMap;

/**
 * Created by joran on 21-1-15.
 */
public class ClientController extends Thread {
    //-------------------------------Fields--------------------------------------------------------
    public static final String INITIAL_NAME = "Name";
    public static final String INITIAL_GROUP = "19";
    public static final String[] INITIAL_EXTENSIONS = new String[0];
    private boolean aiOn = false;
    public boolean connected;
    public HashMap<String, String> lobby = new HashMap<String, String>();
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

    public AI getAi() {
        return ai;
    }

    private AI ai = new AI(this);

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
    //---------------------------Manage lobby--------------------------
    public void addPlayerToLobby(String playername) {
        lobby.put(playername, null);
        guiController.getMainForm().updateLobby();

    }

    public void addPlayerToLobby(String playername, String state) {
        lobby.put(playername, state);
        guiController.getMainForm().updateLobby();
    }

    public void removePlayerFromLobby(String playername) {
        lobby.remove(playername);
        guiController.getMainForm().updateLobby();
    }
    //---------------------------Manage moves--------------------------
    public void doMove(int i, String player) {
        synchronized (board) {
            if (player.equals(clientName)) {
                board.makeMove(i, Disc.Red);
            } else if (player.equals(opponent)) {
                board.makeMove(i, Disc.Yellow);
            } else {
                System.out.println("Invalid playername");
            }
            guiController.getControlForm().repaint();
        }
    }

    public void tryMove(int i, String player) {
        synchronized (board) {
            if (myTurn) {
                connection.getProtocol().sendDoMove(String.valueOf(i));
                myTurn = false;
                guiController.getControlForm().disableHintButton();
                guiController.getControlForm().setGameState("Opponents turn.");
            } else {
                guiController.getControlForm().setGameState("Opponents turn. Not yours");
            }
        }
    }

    public void setMyTurnTrue() {
        myTurn = true;
        guiController.getControlForm().enableHintButton();
        if (aiOn) {
            ai.doMove(myTurn);
        }
        guiController.getControlForm().setGameState("Your turn.");
    }
    //----------------------Manage end game-----------------------------
    public void endGame(String winner) {
        setMyTurn(false);
        setOpponent(null);
        resetBoard();
        guiController.closeControlForm();
        guiController.sendWinnerMessage(winner);
        guiController.getMainForm().switchReadyButton();
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

    public void setAiOn(Boolean b) {
        this.aiOn = b;
    }

    public void newConnection() {
        connection = new Connection(this);
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

    public void sendGlobalMessage(String s) {
        connection.getProtocol().sendGlobalChat(s);
    }

    public void sendLocalMessage(String s) {
        connection.getProtocol().sendLocalChat(s);
    }

    public void sendMessageChatNotEnabeled() {
        guiController.sendMessageChatNotEnabeled();
    }

    //------------------GettersAndSetters--------------------------------------------
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection c) {
        this.connection = c;
    }

    public void setMyTurn(boolean argMyTurn) {
        this.myTurn = argMyTurn;
    }

    public void setClientName(String argClientName) {
        this.clientName = argClientName;
    }

    public void setGroup(String argGroup) {
        this.group = argGroup;
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

    public HashMap<String, String> getLobby() {
        return lobby;
    }

    public void setLobby(HashMap<String, String> argLobby) {
        this.lobby = argLobby;
    }

    public GuiController getGuiController() {
        return guiController;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean argConnected) {
        this.connected = argConnected;
    }

}