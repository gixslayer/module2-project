package server.rooms;

import server.player.Player;
import server.player.PlayerState;

public final class Lobby extends Room {
    public void playerStateChanged(Player player) {
        String name = player.getName();
        PlayerState state = player.getState();

        for (Player p : getPlayers()) {
            // Send the status update to all players in the lobby, excluding the player itself.
            if (p != player) {
                p.getProtocol().sendStateChange(name, state);
            }
        }
    }

    @Override
    public void addPlayer(Player player) {
        // If a client enters the lobby spam him with the current state of all other clients.
        // This method is FAR from pretty, but it *sort of* works.

        super.addPlayer(player);
    }
}
