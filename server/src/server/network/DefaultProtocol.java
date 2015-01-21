package server.network;

import server.Constants;
import server.Main;
import server.matchmaking.Challenger;
import server.player.Player;
import server.player.PlayerState;
import server.rooms.GameRoom;
import findfour.shared.utils.StringUtils;

public final class DefaultProtocol extends Protocol {
    private static final String CMD_READY = "ready_for_game";
    private static final String CMD_DO_MOVE = "do_move";
    private static final String CMD_START_GAME = "start_game";
    private static final String CMD_REQUEST_MOVE = "request_move";
    private static final String CMD_DONE_MOVE = "done_move";
    private static final String CMD_GAME_END = "game_end";
    private static final String CMD_STATE_CHANGE = "state_change";
    private static final String CMD_CHAT = "message";
    private static final String CMD_LOCAL_CHAT = "chat_local";
    private static final String CMD_GLOBAL_CHAT = "chat_global";
    private static final String CMD_CHALLENGE = "challenge";
    private static final String CMD_CHALLENGE_RESPONSE = "challenge_response";
    private static final String ERR_INVALID_MOVE = "error 002";
    private static final String ERR_PLAYER_DISCONNECTED = "error 003";
    private static final String ERR_CANNOT_CHALLENGE = "error 005";
    private static final String ERR_INVALID_CHAT = "error 006";
    private static final String ERR_INVALID_CMD = "error 007";
    private static final String ERR_INVALID_CONTEXT = "error 008";
    private static final String ERR_INVALID_PARAMETER = "error 009";
    private static final String ERR_SYNTAX = "error 010";
    private static final String STATE_INGAME = "game";
    private static final String STATE_INLOBBY = "lobby";
    private static final String STATE_INQUEUE = "lobby_ready";
    private static final String STATE_DISCONNECTED = "offline";

    private final String[] supportedExtensions;
    private final boolean hasChatExt;
    private final boolean hasLobbyExt;
    private final boolean hasChallengeExt;
    private final StateCache stateCache;

    public DefaultProtocol(Player player, String[] extensions) {
        super(player);

        this.supportedExtensions = extensions;
        this.hasChatExt = isExtensionSupported(Constants.EXT_CHAT);
        this.hasLobbyExt = isExtensionSupported(Constants.EXT_LOBBY);
        this.hasChallengeExt = isExtensionSupported(Constants.EXT_CHALLENGE);
        this.stateCache = new StateCache();
    }

    @Override
    public void sendStartGame(String opponent) {
        send("%s %s %s", CMD_START_GAME, player.getName(), opponent);
    }

    @Override
    public void sendRequestMove(String playerName) {
        send("%s %s", CMD_REQUEST_MOVE, playerName);
    }

    @Override
    public void sendNotYourMove() {
        send(ERR_INVALID_CONTEXT);
    }

    @Override
    public void sendInvalidMove() {
        send(ERR_INVALID_MOVE);
    }

    @Override
    public void sendDoneMove(String playerName, int column) {
        send("%s %s %s", CMD_DONE_MOVE, playerName, column);
    }

    @Override
    public void sendGameWon(String winner) {
        send("%s %s", CMD_GAME_END, winner);
    }

    @Override
    public void sendGameDraw() {
        send(CMD_GAME_END);
    }

    @Override
    public void sendOpponentDisconnected() {
        send(ERR_PLAYER_DISCONNECTED);
    }

    @Override
    public void sendStateChange(String playerName, PlayerState state) {
        // Only send if the client supports the lobby extension.
        if (!hasLobbyExt) {
            return;
        }

        // The stateString must have an initial value, but will always be assigned another value.
        String stateString = null;

        if (state == PlayerState.InGame) {
            stateString = STATE_INGAME;
        } else if (state == PlayerState.InLobby) {
            stateString = STATE_INLOBBY;
        } else if (state == PlayerState.InQueue) {
            stateString = STATE_INQUEUE;
        } else if (state == PlayerState.Disconnected) {
            stateString = STATE_DISCONNECTED;
        }

        stateCache.update(playerName, state);

        send("%s %s %s", CMD_STATE_CHANGE, playerName, stateString);
    }

    @Override
    public void sendClientStates() {
        if (!hasLobbyExt) {
            return;
        }

        // If a client enters the lobby spam him with the current state of all other clients.
        // This method is FAR from pretty, but it *sort of* works. A cache is used to perform delta
        // checks to eliminate the number of updates that actually have to be send.
        for (Player p : Main.INSTANCE.getPlayerManager().getAllBut(player)) {
            String name = p.getName();
            PlayerState state = p.getState();

            if (stateCache.differs(name, state)) {
                sendStateChange(name, state);
            }
        }
    }

    @Override
    public void sendChat(String playerName, String message) {
        if (!hasChatExt) {
            return;
        }

        send("%s %s %s", CMD_CHAT, playerName, message);
    }

    @Override
    public void sendChallengeNotify(String playerName) {
        send("%s %s", CMD_CHALLENGE, playerName);















    }

    @Override
    public void handlePacket(String packet) {
        String command = StringUtils.extractCommand(packet, DELIMITER);
        String[] args = StringUtils.extractArgs(packet, DELIMITER, false);

        switch (command) {
            case CMD_READY:
                handleReady();
                break;

            case CMD_DO_MOVE:
                handleDoMove(args);
                break;

            case CMD_LOCAL_CHAT:
                handleLocalChat(packet);
                break;

            case CMD_GLOBAL_CHAT:
                handleGlobalChat(packet);
                break;

            case CMD_CHALLENGE:
                handleChallenge(args);
                break;

            case CMD_CHALLENGE_RESPONSE:
                handleChallengeResponse(args);
                break;

            default:
                send(ERR_INVALID_CMD);
                break;
        }
    }

    @Override
    public boolean supportsChallenging() {
        return hasChallengeExt;
    }

    private void handleReady() {
        if (player.getState() == PlayerState.InLobby) {
            Main.INSTANCE.getMatchMaker().queuePlayer(player);
        } else {
            send(ERR_INVALID_CONTEXT);
        }
    }

    private void handleDoMove(String[] args) {
        if (player.getState() == PlayerState.InGame) {
            if (args.length >= 1) {
                String column = args[0];

                if (isValidCol(column)) {
                    GameRoom room = (GameRoom) player.getRoom();
                    int col = StringUtils.parseInt(column);

                    room.handleMove(player, col);
                } else {
                    send(ERR_INVALID_PARAMETER);
                }
            } else {
                send(ERR_SYNTAX);
            }
        } else {
            send(ERR_INVALID_CONTEXT);
        }
    }

    private void handleLocalChat(String packet) {
        // Eat the packet if the client indicated it does not support chat, but send it anyway.
        if (!hasChatExt) {
            send(ERR_INVALID_CMD);
            return;
        }

        if (packet.matches(CMD_LOCAL_CHAT + DELIMITER + ".*")) {
            String message = packet.substring(packet.indexOf(DELIMITER) + 1);

            if (isValidMessage(message)) {
                player.getRoom().broadcastChat(player, message);
            } else {
                send(ERR_INVALID_CHAT);
            }
        } else {
            send(ERR_SYNTAX);
        }

    }

    private void handleGlobalChat(String packet) {
        // Eat the packet if the client indicated it does not support chat, but send it anyway.
        if (!hasChatExt) {
            send(ERR_INVALID_CMD);
            return;
        }

        if (packet.matches(CMD_GLOBAL_CHAT + DELIMITER + ".*")) {
            String message = packet.substring(packet.indexOf(DELIMITER) + 1);

            if (isValidMessage(message)) {
                String playerName = player.getName();

                for (Player p : Main.INSTANCE.getPlayerManager().getAllBut(player)) {
                    p.getProtocol().sendChat(playerName, message);
                }
            } else {
                send(ERR_INVALID_CHAT);
            }
        } else {
            send(ERR_SYNTAX);
        }
    }

    private void handleChallenge(String[] args) {
        if (!hasChallengeExt) {
            send(ERR_INVALID_CMD);
            return;
        }

        if (player.getState() == PlayerState.InLobby) {
            if (args.length >= 1) {
                String playerName = args[0];

                if (isValidName(playerName)) {
                    if (!Main.INSTANCE.getChallenger().challenge(player, playerName)) {
                        send(ERR_CANNOT_CHALLENGE);
                    }
                } else {
                    send(ERR_INVALID_PARAMETER);
                }
            } else {
                send(ERR_SYNTAX);
            }
        } else {
            send(ERR_INVALID_CONTEXT);
        }
    }

    private void handleChallengeResponse(String[] args) {
        if (!hasChallengeExt) {
            send(ERR_INVALID_CMD);
            return;
        }

        if (player.getState() == PlayerState.InLobby) {
            if (args.length >= 2) {
                String playerName = args[0];
                String answerString = args[1];

                if (isValidName(playerName) && isValidBoolean(answerString)) {
                    boolean answer = answerString.equals("yes");
                    Challenger challenger = Main.INSTANCE.getChallenger();

                    if (!challenger.handleChallengeResponse(player, playerName, answer)) {
                        send(ERR_INVALID_PARAMETER);
                    }
                } else {
                    send(ERR_INVALID_PARAMETER);
                }
            } else {
                send(ERR_SYNTAX);
            }
        } else {
            send(ERR_INVALID_CONTEXT);
        }
    }

    private boolean isValidCol(String col) {
        return col.matches("[0-6]");
    }

    private boolean isValidMessage(String message) {
        return message.matches("");
    }

    private boolean isValidName(String name) {
        return name.matches("\\w+");
    }

    private boolean isValidBoolean(String bool) {
        return bool.matches("yes|no");
    }

    private boolean isExtensionSupported(String extension) {
        for (String supportedExtension : supportedExtensions) {
            if (supportedExtension.equals(extension)) {
                return true;
            }
        }

        return false;
    }

}
