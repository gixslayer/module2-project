package server.network;

import server.player.Player;
import server.player.PlayerState;

public abstract class Protocol {
    public static final char DELIMITER = ' ';

    protected final Player player;

    public Protocol(Player argPlayer) {
        this.player = argPlayer;
    }

    public abstract void handlePacket(String packet);

    public abstract void sendStartGame(String startingPlayer, String otherPlayer);

    public abstract void sendRequestMove(String playerName);

    public abstract void sendNotYourMove();

    public abstract void sendInvalidMove();

    public abstract void sendDoneMove(String playerName, int column);

    public abstract void sendGameWon(String winner);

    public abstract void sendGameDraw();

    public abstract void sendOpponentDisconnected(String name);

    public abstract void sendStateChange(String playerName, PlayerState state);

    public abstract void sendClientStates();

    public abstract void sendGlobalChat(String playerName, String message);

    public abstract void sendLocalChat(String playerName, String message);

    public abstract void sendChallengeNotify(String playerName);

    public abstract void sendAccept();

    public abstract void sendCannotChallenge(String reason);

    public abstract void sendChallengeFailed(String reason);

    public abstract boolean supportsChallenging();

    public abstract String getName();

    protected void send(String packet) {
        player.getClient().send(packet);
    }

    protected void send(String format, Object... args) {
        player.getClient().send(String.format(format, args));
    }

    protected void sendErr(String error, String message) {
        player.getClient().send(String.format("%s %s", error, message));
    }
}
