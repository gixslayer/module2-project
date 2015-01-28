package server.network;

import server.Constants;
import server.Main;
import server.matchmaking.Challenger;
import server.player.Player;
import server.player.PlayerState;
import server.rooms.GameRoom;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;
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
    private static final String CMD_ERROR = "error";
    private static final String ERR_INVALID_MOVE = "error 002";
    private static final String ERR_PLAYER_DISCONNECTED = "error 003";
    private static final String ERR_CANNOT_CHALLENGE = "error 005";
    private static final String ERR_INVALID_CHAT = "error 006";
    private static final String ERR_INVALID_CMD = "error 007";
    private static final String ERR_INVALID_PARAMETER = "error 008";
    private static final String ERR_SYNTAX = "error 009";
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
    public void sendStartGame(String startingPlayer, String otherPlayer) {
        send("%s %s %s", CMD_START_GAME, startingPlayer, otherPlayer);
    }

    @Override
    public void sendRequestMove(String playerName) {
        send("%s %s", CMD_REQUEST_MOVE, playerName);
    }

    @Override
    public void sendNotYourMove() {
        sendErr(ERR_INVALID_CMD, "Not your move");
    }

    @Override
    public void sendInvalidMove() {
        sendErr(ERR_INVALID_MOVE, "Move is invalid");
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
    public void sendOpponentDisconnected(String name) {
        sendErr(ERR_PLAYER_DISCONNECTED, name);
    }

    @Override
    public void sendStateChange(String playerName, PlayerState state) {
        // Only send if the client supports the lobby extension.
        if (!hasLobbyExt) {
            return;
        }

        if (stateCache.differs(playerName, state)) {
            stateCache.update(playerName, state);

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

            send("%s %s %s", CMD_STATE_CHANGE, playerName, stateString);
        }
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
    public void sendGlobalChat(String playerName, String message) {
        if (!hasChatExt) {
            return;
        }

        send("%s %s [global]%s", CMD_CHAT, playerName, message);
    }

    @Override
    public void sendLocalChat(String playerName, String message) {
        if (!hasChatExt) {
            return;
        }

        send("%s %s [local]%s", CMD_CHAT, playerName, message);
    }

    @Override
    public void sendChallengeNotify(String playerName) {
        send("%s %s", CMD_CHALLENGE, playerName);

    }

    @Override
    public void sendAccept() {

    }

    @Override
    public void sendCannotChallenge(String reason) {
        sendErr(ERR_CANNOT_CHALLENGE, reason);
    }

    @Override
    public void sendChallengeFailed(String reason) {
        sendErr(ERR_INVALID_PARAMETER, reason);
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

            case CMD_ERROR:
                handleError(args, packet);
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

    @Override
    public String getName() {
        return "default";
    }

    private void handleReady() {
        if (player.getState() == PlayerState.InLobby) {
            Main.INSTANCE.getMatchMaker().queuePlayer(player);
        } else {
            sendErr(ERR_INVALID_CMD, "You must be in the lobby state");
        }
    }

    private void handleDoMove(String[] args) {
        if (player.getState() != PlayerState.InGame) {
            sendErr(ERR_INVALID_CMD, "You must be in the game state");
        } else if (args.length < 1) {
            sendErr(ERR_SYNTAX, "Expected at least 1 parameter");
        } else if (!isValidCol(args[0])) {
            sendErr(ERR_INVALID_PARAMETER, "col");
        } else {
            GameRoom room = (GameRoom) player.getRoom();
            int column = StringUtils.parseInt(args[0]);

            room.handleMove(player, column);
        }
    }

    private void handleLocalChat(String packet) {
        if (!hasChatExt) {
            sendErr(ERR_INVALID_CMD, "Chat extension not specified during handshake");
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
            sendErr(ERR_SYNTAX, "Expected at least 1 parameter");
        }

    }

    private void handleGlobalChat(String packet) {
        if (!hasChatExt) {
            sendErr(ERR_INVALID_CMD, "Chat extension not specified during handshake");
            return;
        }

        if (packet.matches(CMD_GLOBAL_CHAT + DELIMITER + ".*")) {
            String message = packet.substring(packet.indexOf(DELIMITER) + 1);

            if (isValidMessage(message)) {
                String playerName = player.getName();

                Log.info(LogLevel.Verbose, "[Global chat] %s: %s", playerName, message);

                for (Player p : Main.INSTANCE.getPlayerManager().getAllBut(player)) {
                    p.getProtocol().sendGlobalChat(playerName, message);
                }
            } else {
                send(ERR_INVALID_CHAT);
            }
        } else {
            sendErr(ERR_SYNTAX, "Expected at least 1 parameter");
        }
    }

    private void handleChallenge(String[] args) {
        if (!hasChallengeExt) {
            send(ERR_INVALID_CMD, "Challenge extension not specified during handshake");
            return;
        }

        if (player.getState() != PlayerState.InLobby) {
            sendErr(ERR_INVALID_CMD, "You must be in the lobby state");
        } else if (args.length < 1) {
            sendErr(ERR_SYNTAX, "Expected at least 1 parameter");
        } else if (!isValidName(args[0])) {
            sendErr(ERR_INVALID_PARAMETER, "player-name");
        } else {
            Main.INSTANCE.getChallenger().challenge(player, args[0]);
        }
    }

    private void handleChallengeResponse(String[] args) {
        if (!hasChallengeExt) {
            send(ERR_INVALID_CMD, "Challenge extension not specified during handshake");
            return;
        }

        if (player.getState() != PlayerState.InLobby) {
            sendErr(ERR_INVALID_CMD, "You must be in the lobby state");
        } else if (args.length < 2) {
            sendErr(ERR_SYNTAX, "Expected at least 2 parameters");
        } else if (!isValidName(args[0])) {
            sendErr(ERR_INVALID_PARAMETER, "player-name");
        } else if (!isValidBoolean(args[1])) {
            sendErr(ERR_INVALID_PARAMETER, "boolean-answer");
        } else {
            boolean answer = args[1].equals("yes");
            Challenger challenger = Main.INSTANCE.getChallenger();

            challenger.handleChallengeResponse(player, args[0], answer);
        }
    }

    private void handleError(String[] args, String packet) {
        if (args.length < 1) {
            sendErr(ERR_SYNTAX, "Expected at least 1 parameter");
        } else if (!isValidError(args[0])) {
            sendErr(ERR_INVALID_PARAMETER, "error_code");
        } else {
            int minLength = CMD_ERROR.length() + 4; // delimiter(1) + error_code(3) == 4.
            String message = packet.length() > minLength ? packet.substring(minLength + 1) : "";

            // Assuming our server implementation is correct a client would only send an error if
            // something went wrong on their side. Don't try to act as a hero and 'fix' the
            // client's error as the information is -very- limited or completely implementation
            // specific. Outputting this error is more like a debug feature than anything.
            Log.warning(LogLevel.Minimal, "Client %s reported an error: %s (%s)", player.getName(),
                    message, args[0]);
        }
    }

    private boolean isValidCol(String col) {
        return col.matches("[0-6]");
    }

    private boolean isValidMessage(String message) {
        return message.matches("[a-zA-Z0-9 ]*");
    }

    private boolean isValidName(String name) {
        return name.matches("\\w+");
    }

    private boolean isValidBoolean(String bool) {
        return bool.matches("yes|no");
    }

    private boolean isValidError(String error) {
        return error.matches("\\d{3}");
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
