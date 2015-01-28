package server.network;

import server.Constants;
import server.IllegalInvokeException;
import server.Main;
import server.matchmaking.Challenger;
import server.player.Player;
import server.player.PlayerState;
import server.rooms.GameRoom;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;
import findfour.shared.utils.StringUtils;

/**
 * The implementation of the default protocol specified within the work-group INF-3.
 * @author ciske
 *
 */
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

    /**
     * Creates a new instance of the default protocol.
     * @param player The player which owns this protocol instance
     * @param extensions The extensions the player supports
     */
    //@ requires player != null;
    //@ requires extensions != null;
    public DefaultProtocol(Player player, String[] extensions) {
        super(player);

        this.supportedExtensions = extensions;
        this.hasChatExt = isExtensionSupported(Constants.EXT_CHAT);
        this.hasLobbyExt = isExtensionSupported(Constants.EXT_LOBBY);
        this.hasChallengeExt = isExtensionSupported(Constants.EXT_CHALLENGE);
        this.stateCache = new StateCache();
    }

    /**
     * Send the startGame command to the client to signal a new game has been started.
     * @param startingPlayer the name of the starting player
     * @param otherPlayer the name of the other player
     */
    //@ requires startingPlayer != null;
    //@ requires otherPlayer != null;
    //@ requires startingPlayer != otherPlayer;
    @Override
    public void sendStartGame(String startingPlayer, String otherPlayer) {
        send("%s %s %s", CMD_START_GAME, startingPlayer, otherPlayer);
    }

    /**
     * Send the requestMove command to the client to signal which player has to make the next move.
     * @param playerName the name of the player which has to make the next move
     */
    //@ requires playerName != null;
    @Override
    public void sendRequestMove(String playerName) {
        send("%s %s", CMD_REQUEST_MOVE, playerName);
    }

    /**
     * Send the notYourMove command to the client to signal the player he tried to perform a move
     * when it wasn't his turn.
     */
    @Override
    public void sendNotYourMove() {
        sendErr(ERR_INVALID_CMD, "Not your move");
    }

    /**
     * Send the invalidMove command to the client to signal the player his move wasn't valid.
     */
    @Override
    public void sendInvalidMove() {
        sendErr(ERR_INVALID_MOVE, "Move is invalid");
    }

    /**
     * Send the doneMove command to the client to signal a player has performed a move.
     * @param playerName The name of the player which just made a move
     * @param column The column in which the player made his move
     */
    //@ requires playerName != null;
    //@ requires column >= 0 && column <= 6;
    @Override
    public void sendDoneMove(String playerName, int column) {
        send("%s %s %s", CMD_DONE_MOVE, playerName, column);
    }

    /**
     * Send the gameWon command to the client to signal the game has been won by a player.
     * @param winner The name of the winning player
     */
    //@ requires winner != null;
    @Override
    public void sendGameWon(String winner) {
        send("%s %s", CMD_GAME_END, winner);
    }

    /**
     * Send the gameDraw command to the client to signal the game has has ended in a draw.
     */
    @Override
    public void sendGameDraw() {
        send(CMD_GAME_END);
    }

    /**
     * Send the opponentDisconnected command to the client to signal that one of the players in the
     * current game has disconnected.
     * @param name The name of the player that disconnected
     */
    //@ requires name != null;
    @Override
    public void sendOpponentDisconnected(String name) {
        sendErr(ERR_PLAYER_DISCONNECTED, name);
    }

    /**
     * Send the stateChange command to the client to signal that the state of another player has
     * changed.
     * @param playerName The name of the player that just had the state change
     * @param state The new state of the player
     */
    //@ requires playerName != null;
    //@ requires state != null && state != PlayerState.InitialConnect;
    //@ ensures stateCache.differs(playerName, state) == false;
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

    /**
     * Send all the stateChange commands to the client to build an initial list of the states and
     * names of other clients.
     */
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

    /**
     * Send a global chat message to the client.
     * @param playerName The player who send the chat message
     * @param message The content of the chat message
     */
    //@ requires playerName != null;
    //@ requires message != null;
    @Override
    public void sendGlobalChat(String playerName, String message) {
        if (!hasChatExt) {
            return;
        }

        send("%s %s [global]%s", CMD_CHAT, playerName, message);
    }

    /**
     * Send a local chat message to the client.
     * @param playerName The player who send the chat message
     * @param message The content of the chat message
     */
    //@ requires playerName != null;
    //@ requires message != null;
    @Override
    public void sendLocalChat(String playerName, String message) {
        if (!hasChatExt) {
            return;
        }

        send("%s %s [local]%s", CMD_CHAT, playerName, message);
    }

    /**
     * Send a notification to the client that another player has challenged him.
     * @param playerName
     */
    //@ requires playerName != null && player.getName() != playerName;
    //@ requires hasChallengeExt == true;
    @Override
    public void sendChallengeNotify(String playerName) {
        send("%s %s", CMD_CHALLENGE, playerName);
    }

    /**
     * Should never be called in this Protocol implementation.
     */
    @Override
    public void sendAccept() {
        throw new IllegalInvokeException();
    }

    /**
     * Send a message to the client to notify him he cannot challenge the player he requested to
     * challenge.
     * @param reason The reason why the player could not be challenged
     */
    //@ requires reason != null;
    @Override
    public void sendCannotChallenge(String reason) {
        sendErr(ERR_CANNOT_CHALLENGE, reason);
    }

    /**
     * Send to the client to notify him his challenge response failed.
     * @param reason The reason why the challenge response failed.
     */
    //@ requires reason != null;
    @Override
    public void sendChallengeFailed(String reason) {
        sendErr(ERR_INVALID_PARAMETER, reason);
    }

    /**
     * Handles the raw incoming data.
     * @param packet The raw packet which has to be handled
     */
    //@ requires packet != null;
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

    /**
     * Returns whether this protocol instance supports challenging.
     */
    @Override
    public boolean supportsChallenging() {
        return hasChallengeExt;
    }

    /**
     * Returns the name of this protocol implementation.
     */
    @Override
    public String getName() {
        return "default";
    }

    /**
     * Handles the ready command send by the client which signals he is ready for a matchmaking
     * game.
     */
    private void handleReady() {
        if (player.getState() == PlayerState.InLobby) {
            Main.INSTANCE.getMatchMaker().queuePlayer(player);
        } else {
            sendErr(ERR_INVALID_CMD, "You must be in the lobby state");
        }
    }

    /**
     * Handles the doMove command send by the client which signals he wants to perform a move.
     * @param args The arguments of the command split by the delimiter
     */
    //@ requires args != null;
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

    /**
     * Handles the localChat command send by the client which signals he wants to post a chat
     * message in his current local chat.
     * @param packet The raw packet as received by handlePacket
     */
    //@ requires packet != null;
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

    /**
     * Handles the globalChat command send by the client which signals he wants to post a chat
     * message in the global chat.
     * @param packet The raw packet as received by handlePacket
     */
    //@ requires packet != null;
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

    /**
     * Handles the challenge command send by the client which signals he wants to challenge another
     * player.
     * @param args The arguments of the command split by the delimiter
     */
    //@ requires args != null;
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

    /**
     * Handles the challengeResponse command send by the client which signals he wants to respond to
     * a challenge request.
     * @param args The arguments of the command split by the delimiter.
     */
    //@ requires args != null;
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

    /**
     * Handles the error command send by the client which signals he wants to inform the server he
     * has encountered an error.
     * @param args The arguments of the command split by the delimiter.
     * @param packet The raw packet as received by handlePacket
     */
    //@ requires args != null;
    //@ requires packet != null;
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

    /**
     * Checks if a given column string is in a valid format.
     * @param col The column string
     */
    /*@ pure */
    //@ requires col != null;
    private boolean isValidCol(String col) {
        return col.matches("[0-6]");
    }

    /**
     * Checks if a given message string is in a valid format.
     * @param message The message string
     */
    /*@ pure */
    //@ requires message != null;
    private boolean isValidMessage(String message) {
        return message.matches("[a-zA-Z0-9 ]*");
    }

    /**
     * Checks if a given name string is in a valid format.
     * @param name The name string
     */
    /*@ pure */
    //@ requires name != null;
    private boolean isValidName(String name) {
        return name.matches("\\w+");
    }

    /**
     * Checks if a given boolean string is in a valid format.
     * @param bool The boolean string
     */
    /*@ pure */
    //@ requires bool != null;
    private boolean isValidBoolean(String bool) {
        return bool.matches("yes|no");
    }

    /**
     * Checks if a given error code string is in a valid format.
     * @param error The error code string
     */
    /*@ pure */
    //@ requires error != null;
    private boolean isValidError(String error) {
        return error.matches("\\d{3}");
    }

    /**
     * Checks if a given extension is supported by this protocol instance.
     * @param extension The name of the extension to check
     */
    /*@ pure */
    //@ requires extension != null;
    private boolean isExtensionSupported(String extension) {
        for (String supportedExtension : supportedExtensions) {
            if (supportedExtension.equals(extension)) {
                return true;
            }
        }

        return false;
    }

}
