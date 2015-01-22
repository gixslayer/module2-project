package server.rooms;

import java.util.LinkedList;
import java.util.List;

import server.player.Player;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;

public abstract class Room {
    private final List<Player> players;

    public Room() {
        this.players = new LinkedList<Player>();
    }

    public synchronized void onPlayerDisconnect(Player player) {
        players.remove(player);
    }

    public synchronized void addPlayer(Player player) {
        players.add(player);
    }

    public synchronized void removePlayer(Player player) {
        players.remove(player);
    }

    public synchronized void broadcastChat(Player source, String message) {
        String playerName = source.getName();

        // TODO: Should include the name of the room.
        Log.info(LogLevel.Verbose, "[Local chat] %s: %s", playerName, message);

        for (Player player : players) {
            if (player != source) {
                player.getProtocol().sendChat(playerName, message);
            }
        }
    }

    public List<Player> getPlayers() {
        return players;
    }
}
