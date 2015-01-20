package server.rooms;

import java.util.HashMap;
import java.util.Map;

import server.player.Player;

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

    public void startGame(Player playerA, Player playerB) {
        String roomName = String.format("%svs%s", playerA.getName(), playerB.getName());
        GameRoom room = new GameRoom(roomName, playerA, playerB);

        playerA.moveToGame(room);
        playerB.moveToGame(room);

        gameRooms.put(roomName, room);

        room.begin();
    }

    void endGame(GameRoom room) {
        for (Player player : room.getPlayers()) {
            player.moveToLobby();
        }

        gameRooms.remove(room.getName());
    }
}
