package server.network;

import server.Constants;
import server.Main;
import server.player.Player;
import server.player.PlayerManager;
import server.player.PlayerState;
import findfour.shared.utils.StringUtils;

public final class InitialProtocol extends Protocol {
    private static final String CMD_JOIN = "join";
    private static final String CMD_ACCEPT = "accept";
    private static final String ERR_INVALID_USERNAME = "error 004";
    private static final String ERR_INVALID_CMD = "error 007";
    private static final String ERR_INVALID_PARAMETER = "error 008";
    private static final String ERR_SYNTAX = "error 009";

    private final PlayerManager playerManager;

    public InitialProtocol(Player player) {
        super(player);

        this.playerManager = Main.INSTANCE.getPlayerManager();
    }

    @Override
    public void handlePacket(String packet) {
        String command = StringUtils.extractCommand(packet, DELIMITER);

        if (command.equals(CMD_JOIN)) {
            String[] args = StringUtils.extractArgs(packet, DELIMITER, false);

            handleJoin(args);
        } else {
            send(ERR_INVALID_CMD);
        }
    }

    @Override
    public void sendAccept() {
        send("%s %s %s", CMD_ACCEPT, Constants.GROUP, Constants.SUPPORTED_EXTENSIONS);
    }

    @Override
    public boolean supportsChallenging() {
        return false;
    }

    @Override
    public String getName() {
        return "initial";
    }

    private void handleJoin(String[] args) {
        if (args.length < 2) {
            sendErr(ERR_SYNTAX, "Expected at least 2 parameters");
        } else {
            String requestedName = args[0];
            String group = args[1];
            String[] supportedExtension = new String[args.length - 2];
            System.arraycopy(args, 2, supportedExtension, 0, supportedExtension.length);

            if (!isNameValid(requestedName)) {
                sendErr(ERR_INVALID_USERNAME, "player-name");
            } else if (!isGroupValid(group)) {
                sendErr(ERR_INVALID_PARAMETER, "group-number");
            } else if (!areExtensionsValid(supportedExtension)) {
                sendErr(ERR_INVALID_PARAMETER, "extension-list");
            } else if (!playerManager.completeSession(player, requestedName, group,
                    supportedExtension)) {
                // If completeSession returns false it means that the requested name is taken.
                sendErr(ERR_INVALID_USERNAME, "Name has already been taken");
            }
        }
    }

    private boolean isNameValid(String name) {
        return name.matches("\\w+");
    }

    private boolean isGroupValid(String group) {
        return group.matches("\\d{2}");
    }

    private boolean areExtensionsValid(String[] extensions) {
        for (String extension : extensions) {
            if (!isExtensionValid(extension)) {
                return false;
            }
        }

        return true;
    }

    private boolean isExtensionValid(String extension) {
        return extension.matches("[a-zA-Z][a-zA-Z_]*");
    }

    @Override
    public void sendStartGame(String startingPlayer, String otherPlayer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendRequestMove(String playerName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendNotYourMove() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendInvalidMove() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendDoneMove(String playerName, int column) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendGameWon(String winner) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendGameDraw() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendOpponentDisconnected(String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendStateChange(String playerName, PlayerState state) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendClientStates() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendGlobalChat(String playerName, String message) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendLocalChat(String playerName, String message) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendChallengeNotify(String playerName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendCannotChallenge(String reason) {
        // TODO Auto-generated method stub
    }

    @Override
    public void sendChallengeFailed(String reason) {
        // TODO Auto-generated method stub

    }
}
