package client.network;

import client.ClientController;
import findfour.shared.network.TcpClient;
import findfour.shared.utils.StringUtils;

import java.util.Objects;

public class Protocol {
    TcpClient client;
    ClientController clientController;
    private static final String extentions = "Chat Lobby";
    //To server
    private static final String CMD_JOIN = "join";
    private static final String CMD_READY = "ready_for_game";
    private static final String CMD_DOMOVE = "do_move";
    private static final String CMD_GLOBALCHAT = "chat_global";
    //Both
    private static final String CMD_ERROR = "error";
    private static final String ERR_SYNTAX = "error 010";
    //From server
    private static final String CMD_ACCEPT = "accept";
    private static final String CMD_STATECHANGE = "state_change";
    private static final String CMD_MESSAGE = "message";
    private static final String CMD_START_GAME = "start_game";
    private static final String CMD_REQUEST_MOVE = "request_move";
    private static final String CMD_DONE_MOVE = "done_move";
    private static final String CMD_GAME_END = "game_end";
    private static final Character DELIMITER = ' ';

    public Protocol(TcpClient c, ClientController argClient) {
        this.client = c;
        this.clientController = argClient;
    }

    //Send
    public void sendJoin(String name, String group) {
        client.send(String.format("%s %s %s %s", CMD_JOIN, name, group,extentions));
    }

    public void sendReady() {
        client.send(CMD_READY);
        clientController.resetBoard();
    }

    public void sendGlobalChat(String message){
        client.send(String.format("%s %s", CMD_GLOBALCHAT ,message));
    }

    public void sendDoMove(String col) {
        client.send(String.format("%s %s", CMD_DOMOVE, col));
    }

    public void sendError(String code) {
        client.send(String.format("%s %s", CMD_ERROR, code));
    }

    public void sendError(String code, String message) {
        client.send(String.format("%s %s %s", CMD_ERROR, code, message));
    }

    //Recieve
    public void handlePacket(String packet) {
        String command = StringUtils.extractCommand(packet, DELIMITER);
        String[] args = StringUtils.extractArgs(packet, DELIMITER, false);
        switch (command) {
            case CMD_ACCEPT:
                handleAccept();
                break;
            case CMD_START_GAME:
                handleStartGame(args);
                break;
            case CMD_REQUEST_MOVE:
                handleRequestMove(args);
                break;
            case CMD_DONE_MOVE:
                handleDoneMove(args);
                break;
            case CMD_GAME_END:
                handleGameEnd(args);
                break;
            case CMD_ERROR:
                handleError(args);
                break;
            case CMD_MESSAGE:
                handleMessage(args);
                break;
            case CMD_STATECHANGE:
                handleStateChange(args);
                break;
        }
    }

    //Handle
    public void handleAccept() {
        clientController.setReady(true);
    }

    public void handleStartGame(String[] args) {
        if (args.length < 2) {
            client.send(ERR_SYNTAX);
        } else {
            for (Object a: args){
                System.out.println(a);
                System.out.println(Objects.equals(a, clientController.getClientName()));
            }
            if (Objects.equals(args[0], clientController.getClientName())) {
                clientController.setOpponent(args[1]);
            } else if (Objects.equals(args[1], clientController.getClientName())) {
                clientController.setOpponent(args[0]);
            }
            clientController.resetBoard();
            clientController.getGuiController().openControlForm();
            System.out.println(clientController.getOpponent());
        }
    }

    public void handleRequestMove(String[] args) {
        if (Objects.equals(args[0], clientController.getClientName())){
            clientController.setMyTurnTrue();
        }
    }

    public void handleDoneMove(String[] args) {
        if (args.length < 2) {
            client.send(ERR_SYNTAX);
        } else if (args[1].matches("[0-9]")) {
            clientController.doMove(Integer.parseInt(args[1]), args[0]);
        }
    }

    public void handleGameEnd(String[] args) {
        clientController.getGuiController().closeControlForm();
        clientController.setMyTurn(false);
        clientController.setOpponent(null);
        clientController.resetBoard();
    }

    //TODO Implement support for error codes
    public void handleError(String[] args) {
        if (args.length == 1) {
            System.out.println(String.format("An error has occurred errorcode %s.", args[0]));
        } else {
            System.out.println(String.format(
                    "An error has occurred errorcode %s, and descripton %s.", args[0], args[1]));
        }
    }
    public void handleMessage(String[] args){
        clientController.getGuiController().getMainForm().newMessage(args);
    }
    //TODO check for valid input
    public void handleStateChange(String[] args){
        switch (args[1]){
            case "lobby":
                clientController.addPlayerToLobby(args[0]);
                break;
            case "lobby_ready":
                clientController.addPlayerToLobby(args[0],"ready");
                break;
            case "offline":
                clientController.removePlayerFromLobby(args[0]);
                break;
            case "game":
                clientController.addPlayerToLobby(args[0],"inGame");
                break;
        }
    }

}