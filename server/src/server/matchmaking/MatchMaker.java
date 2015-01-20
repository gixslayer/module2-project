package server.matchmaking;

import java.util.LinkedList;
import java.util.List;

import server.Main;
import server.player.Player;
import server.player.PlayerState;

public final class MatchMaker {
    private final List<Player> queuedPlayers;

    public MatchMaker() {
        this.queuedPlayers = new LinkedList<Player>();
    }

    public synchronized void queuePlayer(Player player) {
        player.setState(PlayerState.InQueue);

        Player opponent = findMatch(player);

        if (opponent == null) {
            queuedPlayers.add(player);
        } else {
            queuedPlayers.remove(opponent);

            Main.INSTANCE.getRoomManager().startGame(player, opponent);
        }
    }

    public synchronized void removeFromQueue(Player player) {
        if (queuedPlayers.contains(player)) {
            queuedPlayers.remove(player);
        }
    }

    private Player findMatch(Player player) {
        // Very simple match making for now. Might expand on this later.
        if (queuedPlayers.size() != 0) {
            return queuedPlayers.get(0);
        } else {
            return null;
        }
    }
}
