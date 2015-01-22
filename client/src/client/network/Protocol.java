package client.network;

import client.server.Client;
import findfour.shared.network.TcpClient;
import findfour.shared.utils.StringUtils;

public class Protocol {
    TcpClient client;
    Client clientm;
    //To server
    private static final String CMD_JOIN = "join";
    private static final String CMD_READY = "ready_for_game";
    private static final String CMD_DOMOVE = "do_move";
    //Both
    private static final String CMD_ERROR = "error";
    private static final String ERR_SYNTAX = "error 010";
    //From server
    private static final String CMD_ACCEPT = "accept";
    private static final String CMD_START_GAME = "start_game";
    private static final String CMD_REQUEST_MOVE = "request_move";
    private static final String CMD_DONE_MOVE = "done_move";
    private static final String CMD_GAME_END = "game_end";
    private static final Character DELIMITER = ' ';

    public Protocol(TcpClient c, Client argClient) {
        this.client = c;
        this.clientm = argClient;
    }

    //Send
    public void sendJoin(String name, String group) {
        client.send(String.format("%s %s %s", CMD_JOIN, name, group));
    }

    public void sendReady() {
        client.send(CMD_READY);
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
                handleRequestMove();
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
        }
    }

    //Handle
    public void handleAccept() {
        clientm.setReady(true);
    }

    public void handleStartGame(String[] args) {
        if (args.length < 2) {
            client.send(ERR_SYNTAX);
        } else {
            if (args[0] == clientm.getClientName()) {
                clientm.setOpponent(args[0]);
            } else if (args[1] == clientm.getClientName()) {
                clientm.setOpponent(args[1]);
            }
            clientm.resetBoard();
        }
    }

    public void handleRequestMove() {
        clientm.setMyTurn(true);
    }

    public void handleDoneMove(String[] args) {
        if (args.length < 2) {
            client.send(ERR_SYNTAX);
        } else if (args[1].matches("[0-9]")
                && (args[0].equals(clientm.getClientName()) || args[0]
                        .equals(clientm.getOpponent()))) {
            clientm.doMove(Integer.parseInt(args[1]), args[0]);
        }
    }

    public void handleGameEnd(String[] args) {
        clientm.setOpponent(null);
        clientm.resetBoard();
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

}