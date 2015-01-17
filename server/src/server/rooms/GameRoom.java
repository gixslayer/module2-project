package server.rooms;

import findfour.shared.game.GameState;

public final class GameRoom extends Room {
    private final GameState gameState;

    public GameRoom() {
        this.gameState = new GameState();
    }
}
