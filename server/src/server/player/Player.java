package server.player;

import server.network.Protocol;
import server.rooms.Room;
import findfour.shared.network.TcpServer;

public final class Player {
    public static final String INITIAL_NAME = "<unknown>";

    private final TcpServer.Client client;
    private String name;
    private PlayerState state;
    private Protocol protocol;
    private Room currentRoom;

    public Player(TcpServer.Client argClient) {
        this.client = argClient;
        this.name = INITIAL_NAME;
        this.state = PlayerState.InitialConnect;
        this.currentRoom = null; // TODO: Should there be a 'void' room for players which have not
        // yet completed the handshake?
        // Set protocol to the 'initial' protocol, which only performs the handshake.
    }

    public TcpServer.Client getClient() {
        return client;
    }

    void setName(String argName) {
        name = argName;
    }

    public void setState(PlayerState argState) {
        state = argState;
    }

    public String getName() {
        return name;
    }

    public PlayerState getState() {
        return state;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Room getRoom() {
        return currentRoom;
    }
}
