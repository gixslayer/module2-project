package server.network;

import server.player.Player;

public abstract class Protocol {
    public static final char DELIMITER = ' ';

    protected final Player player;

    public Protocol(Player argPlayer) {
        this.player = argPlayer;
    }

    public abstract void handlePacket(String packet);

    public abstract void sendStartGame(String opponent);

    public abstract void sendRequestMove(String playerName);

    public abstract void sendNotYourMove();

    public abstract void sendInvalidMove();

    public abstract void sendDoneMove(String playerName, int column);

    public abstract void sendGameWon(String winner);

    public abstract void sendGameDraw();

    protected void send(String packet) {
        player.getClient().send(packet);
    }
}
