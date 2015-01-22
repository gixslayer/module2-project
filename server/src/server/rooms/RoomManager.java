package server.rooms;

import java.util.HashMap;
import java.util.Map;

import server.player.Player;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;

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
        String roomName = String.format("%s:%s", playerA.getName(), playerB.getName());
        GameRoom room = new GameRoom(roomName, playerA, playerB);

        playerA.moveToGame(room);
        playerB.moveToGame(room);

        gameRooms.put(roomName, room);

        Log.info(LogLevel.Normal, "Starting game between %s and %s", playerA.getName(),
                playerB.getName());

        room.begin();
    }

    void endGame(GameRoom room) {
        String roomName = room.getName();
        String playerA = roomName.substring(0, roomName.indexOf(':'));
        String playerB = roomName.substring(roomName.indexOf(':') + 1);

        Log.info(LogLevel.Normal, "The game between %s and %s has ended", playerA, playerB);

        for (Player player : room.getPlayers()) {
            player.moveToLobby();
        }

        gameRooms.remove(roomName);
    }
}
