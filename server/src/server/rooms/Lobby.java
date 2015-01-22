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
        // When a player joins the lobby send him the states of all other clients, which is
        // required for the lobby extension.
        player.getProtocol().sendClientStates();

        super.addPlayer(player);
    }
}
