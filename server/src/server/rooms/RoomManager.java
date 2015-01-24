package server.rooms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import server.player.Player;
import server.player.PlayerState;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;

public final class RoomManager {
    private final Lobby lobby;
    private final Map<String, GameRoom> gameRooms;
    private final Object syncRoot = new Object();

    public RoomManager() {
        this.lobby = new Lobby();
        this.gameRooms = new HashMap<String, GameRoom>();
    }

    public Lobby getLobby() {
        return lobby;
    }

    public List<GameRoom> getGameRooms() {
        List<GameRoom> result = new ArrayList<GameRoom>();

        synchronized (syncRoot) {
            for (Map.Entry<String, GameRoom> entry : gameRooms.entrySet()) {
                result.add(entry.getValue());
            }
        }

        return result;
    }

    public void startGame(Player playerA, Player playerB) {
        String roomName = String.format("%s-%s", playerA.getName(), playerB.getName());
        GameRoom room = new GameRoom(roomName, playerA, playerB);

        // Hack to ensure both players receive the state changed to in game, as the lobby extension
        // is really messy. Without this hack, on the end of a game a player could receive a state
        // switch to in game immediately followed by a state change to in lobby for his opponent.
        playerA.getProtocol().sendStateChange(playerB.getName(), PlayerState.InGame);
        playerB.getProtocol().sendStateChange(playerA.getName(), PlayerState.InGame);

        playerA.moveToGame(room);
        playerB.moveToGame(room);

        synchronized (syncRoot) {
            gameRooms.put(roomName, room);
        }

        Log.info(LogLevel.Normal, "Starting game between %s and %s", playerA.getName(),
                playerB.getName());

        room.begin();
    }

    void endGame(GameRoom room) {
        String roomName = room.getName();
        String playerA = roomName.substring(0, roomName.indexOf('-'));
        String playerB = roomName.substring(roomName.indexOf('-') + 1);

        Log.info(LogLevel.Normal, "The game between %s and %s has ended", playerA, playerB);

        for (Player player : room.getPlayers()) {
            player.moveToLobby();
        }

        synchronized (syncRoot) {
            gameRooms.remove(roomName);
        }
    }
}
