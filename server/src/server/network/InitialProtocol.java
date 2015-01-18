package server.network;

import server.Constants;
import server.Main;
import server.player.Player;
import server.player.PlayerManager;
import findfour.shared.utils.StringUtils;

public final class InitialProtocol extends Protocol {
    private static final String CMD_JOIN = "join";
    private static final String CMD_ACCEPT = "accept";
    private static final String ERR_INVALID_USERNAME = "error 004";
    private static final String ERR_INVALID_CMD = "error 007";
    private static final String ERR_INVALID_PARAMETER = "error 009";
    private static final String ERR_SYNTAX = "error 010";

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

    private void handleJoin(String[] args) {
        if (args.length < 2) {
            player.getClient().send(ERR_SYNTAX);
        } else {
            String requestedName = args[0];
            String group = args[1];
            String[] supportedExtension = new String[args.length - 2];
            System.arraycopy(args, 2, supportedExtension, 0, supportedExtension.length);

            if (!isNameValid(requestedName)) {
                send(ERR_INVALID_USERNAME);
            } else if (!isGroupValid(group)) {
                send(ERR_INVALID_PARAMETER);
            } else if (!areExtensionsValid(supportedExtension)) {
                send(ERR_INVALID_PARAMETER);
            } else {
                playerManager.completeSession(player, requestedName, group, supportedExtension);

                sendAccept();
            }
        }
    }

    private boolean isNameValid(String name) {
        if (!name.matches("\\w+")) {
            return false;
        }

        return playerManager.isPlayerNameAvailable(name);
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

    private void sendAccept() {
        String packet =
                String.format("%s %s %s", CMD_ACCEPT, Constants.GROUP,
                        Constants.SUPPORTED_EXTENSIONS);

        send(packet);
    }

    private boolean isExtensionValid(String extension) {
        return extension.matches("[a-zA-Z][a-zA-Z_]*");
    }

    @Override
    public void sendStartGame(String opponent) {
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
}
