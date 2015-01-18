package server.rooms;

import java.util.HashMap;
import java.util.Map;

import server.player.Player;
import server.player.PlayerState;

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

        playerA.setState(PlayerState.InGame);
        playerB.setState(PlayerState.InGame);

        lobby.removePlayer(playerA);
        lobby.removePlayer(playerB);

        playerA.setRoom(room);
        playerB.setRoom(room);

        gameRooms.put(roomName, room);

        room.begin();
    }

    void endGame(GameRoom room) {
        for (Player player : room.getPlayers()) {
            player.setState(PlayerState.InLobby);
            player.setRoom(lobby);

            lobby.addPlayer(player);
        }

        gameRooms.remove(room.getName());
    }
}
