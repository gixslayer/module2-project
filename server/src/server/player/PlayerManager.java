package server.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import server.Main;
import server.network.DefaultProtocol;
import server.network.Protocol;
import findfour.shared.ArgumentException;
import findfour.shared.ArgumentNullException;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;
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
            if (nameToPlayerMapping.containsKey(name)) {
                return false;
            }

            Protocol currentProtocol = player.getProtocol();
            Protocol protocol = getProtocol(player, group, extensions);
            String extensionsString = formatExtensions(extensions);

            // Moved send accept here as putting the player in the lobby would trigger sending
            // client states to the player state (if it supports the Lobby extension) before
            // sending the accept packet.
            currentProtocol.sendAccept();

            player.setName(name);
            player.setGroup(group);
            player.setExtensions(extensionsString);
            player.setProtocol(protocol);
            player.moveToLobby();

            initialSessions.remove(player);
            nameToPlayerMapping.put(name, player);

            Log.info(LogLevel.Verbose, "Completed handshake with client %s from group %s", name,
                    group);
            Log.debug("ClientInfo %s: protocol %s, extensions %s", name, protocol.getName(),
                    extensionsString);

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

    /*@ pure */
    public boolean hasSession(TcpServer.Client client) {
        synchronized (syncRoot) {
            return clientToPlayerMapping.containsKey(client);
        }
    }

    public boolean hasSession(String name) {
        synchronized (syncRoot) {
            return nameToPlayerMapping.containsKey(name);
        }
    }

    public Player get(String name) {
        if (name == null) {
            throw new ArgumentNullException("name");
        }

        synchronized (syncRoot) {
            if (!nameToPlayerMapping.containsKey(name)) {
                throw new ArgumentException("name",
                        "Could not find a player with the specified name");
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
                throw new ArgumentException("client",
                        "Could not find a player mapped to the specified client");
            }

            return clientToPlayerMapping.get(client);
        }
    }

    public Player getIfExists(String name) {
        synchronized (syncRoot) {
            if (nameToPlayerMapping.containsKey(name)) {
                return nameToPlayerMapping.get(name);
            } else {
                return null;
            }
        }
    }

    public List<Player> getAllBut(Player player) {
        List<Player> result = new ArrayList<Player>();

        synchronized (syncRoot) {
            for (Map.Entry<String, Player> entry : nameToPlayerMapping.entrySet()) {
                if (entry.getValue() != player) {
                    result.add(entry.getValue());
                }
            }
        }

        return result;
    }

    public List<Player> getAll() {
        List<Player> result = new ArrayList<Player>(nameToPlayerMapping.size());

        synchronized (syncRoot) {
            for (Map.Entry<String, Player> entry : nameToPlayerMapping.entrySet()) {
                result.add(entry.getValue());
            }
        }

        return result;
    }

    private String formatExtensions(String[] extensions) {
        if (extensions.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        result.append(extensions[0]);

        for (int i = 1; i < extensions.length; i++) {
            result.append(' ');
            result.append(extensions[i]);
        }

        return result.toString();
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

        if (player.getProtocol().supportsChallenging()) {
            Main.INSTANCE.getChallenger().handlePlayerDisconnect(player);
        }
    }

    private Protocol getProtocol(Player player, String group, String[] extensions) {
        //if (group.equals(Constants.GROUP)) {
        // NOTE: Return our custom protocol here if we ever decide to implement one.
        //}

        return new DefaultProtocol(player, extensions);
    }

}
