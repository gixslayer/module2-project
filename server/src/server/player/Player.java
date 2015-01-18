package server.player;

import server.network.InitialProtocol;
import server.network.Protocol;
import server.rooms.Room;
import findfour.shared.game.Disc;
import findfour.shared.network.TcpServer;

public final class Player {
    public static final String INITIAL_NAME = "<unknown>";
    public static final String INITIAL_GROUP = "<unknown>";
    public static final String[] INITIAL_EXTENSIONS = new String[0];

    private final TcpServer.Client client;
    private String name;
    private PlayerState state;
    private Protocol protocol;
    private Room currentRoom;
    private String group;
    private Disc color;
    private String[] supportedExtensions;

    public Player(TcpServer.Client argClient) {
        this.client = argClient;
        this.name = INITIAL_NAME;
        this.state = PlayerState.InitialConnect;
        this.protocol = new InitialProtocol(this);
        this.currentRoom = null;
        this.group = INITIAL_GROUP;
        this.color = Disc.None;
        this.supportedExtensions = INITIAL_EXTENSIONS;
    }

    public TcpServer.Client getClient() {
        return client;
    }

    void setName(String argName) {
        name = argName;
    }

    void setGroup(String argGroup) {
        group = argGroup;
    }

    void setExtensions(String[] extensions) {
        supportedExtensions = extensions;
    }

    void setProtocol(Protocol argProtocol) {
        protocol = argProtocol;
    }

    public void setState(PlayerState argState) {
        state = argState;
    }

    public void setRoom(Room room) {
        currentRoom = room;
    }

    public void setColor(Disc argColor) {
        color = argColor;
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

    public String getGroup() {
        return group;
    }

    public Disc getColor() {
        return color;
    }

    public String[] getExtensions() {
        return supportedExtensions;
    }
}
