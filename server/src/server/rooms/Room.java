package server.rooms;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import server.player.Player;

public abstract class Room {
    private final List<Player> players;

    public Room() {
        this.players = new LinkedList<Player>();
    }

    public Iterator<Player> getPlayers() {
        return players.iterator();
    }
}
