package server.rooms;

import java.util.LinkedList;
import java.util.List;

import server.player.Player;

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

    public List<Player> getPlayers() {
        return players;
    }
}
