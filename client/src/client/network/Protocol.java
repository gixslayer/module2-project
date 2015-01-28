package client.network;

import client.ClientController;
import findfour.shared.network.TcpClient;
import findfour.shared.utils.StringUtils;

import java.util.Objects;

public class Protocol {
    //----------------------------------Fields--------------------------------------------------------------------------
    TcpClient client;
    ClientController clientController;
    private static final String EXTENSIONS = "Chat Lobby";
    //To server
    private static final String CMD_JOIN = "join";
    private static final String CMD_READY = "ready_for_game";
    private static final String CMD_DOMOVE = "do_move";
    private static final String CMD_GLOBALCHAT = "chat_global";
    private static final String CMD_LOCALCHAT = "chat_local";
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

    //----------------------------------Constructor---------------------------------------------------------------------
    public Protocol(TcpClient c, ClientController argClient) {
        this.client = c;
        this.clientController = argClient;
    }
    //----------------------------------Methods-------------------------------------------------------------------------
    //Send
    public void sendJoin(String name, String group) {
        client.send(String.format("%s %s %s %s", CMD_JOIN, name, group, EXTENSIONS));
    }

    public void sendReady() {
        client.send(CMD_READY);
        clientController.resetBoard();
    }

    public void sendGlobalChat(String message) {
        if (clientController.getConnection().isChatEnabeled()) {
            client.send(String.format("%s %s", CMD_GLOBALCHAT, message));
        }
    }

    public void sendLocalChat(String message) {
        if (clientController.getConnection().isChatEnabeled()) {
            client.send(String.format("%s %s", CMD_LOCALCHAT, message));
        }
    }

    public void displayMessageChatNotAvailable() {
        clientController.sendMessageChatNotEnabeled();
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
                handleAccept(args);
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
    public void handleAccept(String[] args) {
        if (args[1].equals("Chat") || args[2].equals("Chat") || args[3].equals("Chat")) {
            clientController.getConnection().setChatEnabeled(true);
        }
    }

    public void handleStartGame(String[] args) {
        if (args.length < 2) {
            client.send(ERR_SYNTAX);
        } else {
            for (Object a : args) {
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
        if (Objects.equals(args[0], clientController.getClientName())) {
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
        if (args.length > 0) {
            clientController.endGame(args[0]);
        } else {
            clientController.endGame(null);
        }
    }

    public void handleError(String[] args) {
        if (args.length == 1) {
            System.out.println(String.format("An error has occurred errorcode %s.", args[0]));
        } else {
            System.out.println(String.format(
                    "An error has occurred errorcode %s, and descripton %s.", args[0], args[1]));
        }
    }

    public void handleMessage(String[] args) {
        clientController.getGuiController().getMainForm().newMessage(args);
        if (clientController.getGuiController().getControlForm() != null) {
            clientController.getGuiController().getControlForm().newMessage(args);
        }
    }

    public void handleStateChange(String[] args) {
        switch (args[1]) {
            case "lobby":
                clientController.addPlayerToLobby(args[0]);
                break;
            case "lobby_ready":
                clientController.addPlayerToLobby(args[0], "ready");
                break;
            case "offline":
                clientController.removePlayerFromLobby(args[0]);
                break;
            case "game":
                clientController.addPlayerToLobby(args[0], "inGame");
                break;
        }
    }

}