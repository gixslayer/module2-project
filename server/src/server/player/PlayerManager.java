package server.player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import server.Main;
import server.network.DefaultProtocol;
import server.network.Protocol;
import findfour.shared.ArgumentException;
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
                throw new ArgumentException("client", "Client already has a session");
            }

            initialSessions.add(player);
            clientToPlayerMapping.put(client, player);
        }
    }

    public boolean completeSession(Player player, String name, String group, String[] extensions) {
        synchronized (syncRoot) {
            if (!nameToPlayerMapping.containsKey(name)) {
                return false;
            }

            player.setName(name);
            player.setGroup(group);
            player.setExtensions(extensions);
            player.setProtocol(getProtocol(player, group, extensions));
            player.moveToLobby();

            initialSessions.remove(player);
            nameToPlayerMapping.put(name, player);

            return true;
        }
    }

    public void endSession(TcpServer.Client client) {
        synchronized (syncRoot) {
            Player player = clientToPlayerMapping.get(client);
            clientToPlayerMapping.remove(client);

            if (player.getState() == PlayerState.InitialConnect) {
                initialSessions.remove(player);
            } else {
                handleDisconnect(player);

                nameToPlayerMapping.remove(player.getName());

                /*
                 * Finally set the player state to disconnected. This will notify all clients in 
                 * the lobby of this client's disconnect.
                 */
                player.setState(PlayerState.Disconnected);
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

    private void handleDisconnect(Player player) {
        PlayerState state = player.getState();

        // If a player disconnects while in a room (game/lobby), notify that room.
        if (state == PlayerState.InGame || state == PlayerState.InLobby) {
            player.getRoom().onPlayerDisconnect(player);
        }

        // If a player disconnects while in a queue, remove him from the queue.
        if (state == PlayerState.InQueue) {
            Main.INSTANCE.getMatchMaker().removeFromQueue(player);
        }
    }

    private Protocol getProtocol(Player player, String group, String[] extensions) {
        //if (group.equals(Constants.GROUP)) {
        // TODO: Return our custom protocol.
        //}

        return new DefaultProtocol(player, extensions);
    }
}
