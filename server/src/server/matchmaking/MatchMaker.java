package server.matchmaking;

import java.util.LinkedList;
import java.util.List;

import server.Main;
import server.player.Player;
import server.player.PlayerState;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;

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

            Log.info(LogLevel.Verbose, "Added %s to the matchmaking queue", player.getName());
        } else {
            Log.info(LogLevel.Verbose, "Found a match for the players %s and %s", player.getName(),
                    opponent.getName());

            queuedPlayers.remove(opponent);

            Main.INSTANCE.getRoomManager().startGame(player, opponent);
        }
    }

    public synchronized void removeFromQueue(Player player) {
        queuedPlayers.remove(player);

        Log.info(LogLevel.Verbose, "Removed %s from the matchmaking queue", player.getName());
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
