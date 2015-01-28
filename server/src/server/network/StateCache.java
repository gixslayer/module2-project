package server.network;

import java.util.HashMap;
import java.util.Map;

import server.player.PlayerState;

final class StateCache {
    private final Map<String, PlayerState> cache;

    StateCache() {
        this.cache = new HashMap<String, PlayerState>();
    }

    /*@ pure */
    boolean differs(String playerName, PlayerState state) {
        if (!cache.containsKey(playerName)) {
            return true;
        }

        return cache.get(playerName) != state;
    }

    void update(String playerName, PlayerState state) {
        if (state == PlayerState.Disconnected) {
            cache.remove(playerName);
        } else {
            cache.put(playerName, state);
        }
    }
}
