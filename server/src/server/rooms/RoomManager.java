package server.rooms;

import java.util.HashMap;
import java.util.Map;

public final class RoomManager {
    private final Lobby lobby;
    private final Map<String, GameRoom> gameRooms;

    public RoomManager() {
        this.lobby = new Lobby();
        this.gameRooms = new HashMap<String, GameRoom>();
    }

    public Lobby getLobby() {
        return lobby;
    }
}
