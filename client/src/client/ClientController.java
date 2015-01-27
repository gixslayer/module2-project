package client;

import client.GUI.GuiController;
import client.network.Connection;
import findfour.shared.game.Board;
import findfour.shared.game.Disc;

import java.util.HashSet;

/**
 * Created by joran on 21-1-15.
 */
public class ClientController extends Thread {
    //-------------------------------Fields--------------------------------------------------------
    public static final String INITIAL_NAME = "name";
    public static final String INITIAL_GROUP = "19";
    public static final String[] INITIAL_EXTENSIONS = new String[0];
    public boolean connected;
    public HashSet<String> lobby = new HashSet<String>();
    public GuiController guiController = new GuiController(this);
    private String clientName;
    private String group;
    private String[] supportedExtensions;
    private Connection connection = new Connection(this);
    private boolean ready = false;
    private String opponent;
    //TODO set myturn false
    private boolean myTurn = true;
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

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void addPlayerToLobby(String playername){
        if (!lobby.contains(playername)){
            lobby.add(playername);
            guiController.getMainForm().updateLobby();
        }
    }
    public void addPlayerToLobby(String playername, String state){
        if (!lobby.contains(playername)){
            lobby.add(String.format("%s ~%s",playername,state));
            guiController.getMainForm().updateLobby();
        }
    }
    public void removePlayerFromLobby(String playername){
        if (lobby.contains(playername)){
            lobby.remove(playername);
            guiController.getMainForm().updateLobby();
        }
    }
    public void doMove(int i, String player) {
        synchronized (board){
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
    public void tryMove(int i, String player){
        synchronized (board) {
            if (myTurn) {
                connection.getProtocol().sendDoMove(String.valueOf(i));
                myTurn = false;
                guiController.getControlForm().setGameState("Opponents turn.");
            } else {
                guiController.getControlForm().setGameState("Opponents turn. Not yours");
            }
        }
    }
    public void setMyTurnTrue(){
        myTurn = true;
        guiController.getControlForm().setGameState("Your turn.");
    }
    public void sendGlobalMessage(String s){
        connection.getProtocol().sendGlobalChat(s);
    }

    public GuiController getGuiController() {
        return guiController;
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

    public void setConnection(Connection c){ this.connection = c; }

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

    public HashSet<String> getLobby() {
        return lobby;
    }

    public void setLobby(HashSet<String> lobby) {
        this.lobby = lobby;
    }
}