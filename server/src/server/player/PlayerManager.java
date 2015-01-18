package server.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import server.Main;
import server.network.DefaultProtocol;
import server.network.Protocol;
import server.rooms.Lobby;
import findfour.shared.ArgumentNullException;
import findfour.shared.network.TcpServer;

public final class PlayerManager {
    private final Object syncRoot;
    // Map player names (which are unique according to the protocol specs) to the Player instance.
    private final Map<String, Player> nameToPlayerMapping;
    // Map clients to the Player instance.
    private final Map<TcpServer.Client, Player> clientToPlayerMapping;
    // Contains references to all Player instances which have not yet performed the handshake and
    // thus do not yet have a name.
    private final List<Player> initialSessions;

    public PlayerManager() {
        this.syncRoot = new Object();
        this.nameToPlayerMapping = new HashMap<String, Player>();
        this.clientToPlayerMapping = new HashMap<TcpServer.Client, Player>();
        this.initialSessions = new LinkedList<Player>();
    }

    public void createSession(TcpServer.Client client) {
        Player player = new Player(client);

        synchronized (syncRoot) {
            if (clientToPlayerMapping.containsKey(client)) {
                // TODO: Throw an exception, client already has a session.
            }

            initialSessions.add(player);
            clientToPlayerMapping.put(client, player);
        }
    }

    public void completeSession(Player player, String name, String group, String[] extensions) {
        Lobby lobby = Main.INSTANCE.getRoomManager().getLobby();

        player.setName(name);
        player.setGroup(group);
        player.setExtensions(extensions);
        player.setState(PlayerState.InLobby);
        player.setProtocol(getProtocol(player, group, extensions));
        player.setRoom(lobby);

        lobby.addPlayer(player);

        synchronized (syncRoot) {
            initialSessions.remove(player);
            nameToPlayerMapping.put(name, player);
        }
    }

    public void endSession(TcpServer.Client client) {
        synchronized (syncRoot) {
            Player player = clientToPlayerMapping.get(client);
            clientToPlayerMapping.remove(client);

            if (player.getState() == PlayerState.InitialConnect) {
                initialSessions.remove(player);
            } else {
                nameToPlayerMapping.remove(player.getName());
            }
        }
    }

    public boolean hasSession(TcpServer.Client client) {
        synchronized (syncRoot) {
            return clientToPlayerMapping.containsKey(client);
        }
    }

    public Player get(String name) {
        if (name == null) {
            throw new ArgumentNullException("name");
        }

        synchronized (syncRoot) {
            if (!nameToPlayerMapping.containsKey(name)) {
                throw new NoSuchPlayerException("Could not find a player with the name: " + name);
            }

            return nameToPlayerMapping.get(name);
        }
    }

    public Player get(TcpServer.Client client) {
        if (client == null) {
            throw new ArgumentNullException("client");
        }

        synchronized (syncRoot) {
            if (!clientToPlayerMapping.containsKey(client)) {
                throw new NoSuchPlayerException(
                        "Could not find a player mapped to the specified client");
            }

            return clientToPlayerMapping.get(client);
        }
    }

    public boolean isPlayerNameAvailable(String name) {
        // FIXME: It is possible this method is called twice by two different clients before the
        // client session is completed, which would allow both clients to use the same name which
        // would crash on the second client's completeSession.
        return !nameToPlayerMapping.containsKey(name);
    }

    private Protocol getProtocol(Player player, String group, String[] extensions) {
        //if (group.equals(Constants.GROUP)) {
        // TODO: Return our custom protocol.
        //}

        return new DefaultProtocol(player, extensions);
    }
}
