package server.player;

public enum PlayerState {
    /**
     * Initially connected, but has not yet performed the handshake.
     */
    InitialConnect,
    /**
     * Currently in a game.
     */
    InGame,
    /**
     * Currently in the lobby.
     */
    InLobby,
    /**
     * Currently waiting for a game.
     */
    InQueue,
    /**
     * Player has disconnected.
     */
    Disconnected
}
