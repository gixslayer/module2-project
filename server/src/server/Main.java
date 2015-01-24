package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import server.commands.CommandHandler;
import server.commands.CommandInvoker;
import server.matchmaking.Challenger;
import server.matchmaking.MatchMaker;
import server.player.Player;
import server.player.PlayerManager;
import server.rooms.GameRoom;
import server.rooms.RoomManager;
import findfour.shared.events.EventHandler;
import findfour.shared.logging.ConsoleListener;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;
import findfour.shared.network.TcpServer;
import findfour.shared.utils.StringUtils;

public final class Main {
    public static final Main INSTANCE = new Main();

    private static final char COMMAND_DELIMITER = ' ';

    private final TcpServer server;
    private final PlayerManager playerManager;
    private final RoomManager roomManager;
    private final MatchMaker matchMaker;
    private final Challenger challenger;
    private final BufferedReader input;
    private final CommandInvoker commandInvoker;
    private boolean keepRunning;

    private Main() {
        this.server = new TcpServer();
        this.playerManager = new PlayerManager();
        this.roomManager = new RoomManager();
        this.matchMaker = new MatchMaker();
        this.challenger = new Challenger();
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.commandInvoker = new CommandInvoker();
        this.keepRunning = true;
    }

    private void start() {
        initialize();

        while (keepRunning) {
            try {
                String command = input.readLine().trim();

                handleCommand(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cleanup();
    }

    private void initialize() {
        Log.setLogLevel(LogLevel.Verbose);
        Log.setDebugEnabled(true);
        Log.registerListener(new ConsoleListener());

        server.registerEventHandlers(this);
        commandInvoker.registerHandlers(this);
    }

    private void cleanup() {
        if (server.isRunning()) {
            server.stop();
        }
    }

    private void handleCommand(String rawCommand) {
        String command = StringUtils.extractCommand(rawCommand, COMMAND_DELIMITER);
        String[] args = StringUtils.extractArgs(rawCommand, COMMAND_DELIMITER, true);

        commandInvoker.invoke(command, args);
    }

    //----- Command handlers -----
    @CommandHandler
    private void cmdStart(int port) {
        if (port < 0 || port >= 65536) {
            Log.error(LogLevel.Minimal, "Port must be within range [0, 65536).");
        } else if (!server.isRunning()) {
            server.start(port);
        } else {
            Log.info(LogLevel.Minimal, "Server is already running");
        }
    }

    @CommandHandler
    private void cmdStop() {
        if (server.isRunning()) {
            server.stop();
        } else {
            Log.info(LogLevel.Minimal, "Server wasn't running");
        }
    }

    @CommandHandler
    private void cmdQuit() {
        keepRunning = false;
    }

    @CommandHandler
    private void cmdSetLogLevel(String level) {
        if (level.equals("off")) {
            Log.setLogLevel(LogLevel.Off);
            Log.info(LogLevel.Minimal, "Set log level to: %s", level);
        } else if (level.equals("minimal")) {
            Log.setLogLevel(LogLevel.Minimal);
            Log.info(LogLevel.Minimal, "Set log level to: %s", level);
        } else if (level.equals("normal")) {
            Log.setLogLevel(LogLevel.Normal);
            Log.info(LogLevel.Minimal, "Set log level to: %s", level);
        } else if (level.equals("verbose")) {
            Log.setLogLevel(LogLevel.Verbose);
            Log.info(LogLevel.Minimal, "Set log level to: %s", level);
        } else {
            Log.error(LogLevel.Minimal, "Unknown log level: %s", level);
        }
    }

    @CommandHandler
    private void cmdDebugMode(boolean value) {
        Log.setDebugEnabled(value);

        Log.info(LogLevel.Minimal, "%s debug mode", value ? "Enabled" : "Disabled");
    }

    @CommandHandler
    private void cmdListClients() {
        for (Player player : playerManager.getAll()) {
            Log.info(LogLevel.Minimal, "%s, state: %s, group: %s, protocol: %s", player.getName(),
                    player.getState(), player.getGroup(), player.getProtocol().getName());
        }
    }

    @CommandHandler
    private void cmdListGames() {
        for (GameRoom game : roomManager.getGameRooms()) {
            Log.info(LogLevel.Minimal, "%s (%d spectators)", game.getName(),
                    game.getSpectatorCount());
        }
    }

    @CommandHandler
    private void cmdClientInfo(String name) {
        Player player = playerManager.getIfExists(name);

        if (player != null) {
            String state = player.getState().toString();
            String group = player.getGroup();
            String room = player.getRoom().getName();
            String protocol = player.getProtocol().getName();
            String extensions = player.getExtensions();

            Log.info(LogLevel.Minimal,
                    "Name: %s, state: %s, group: %s, room: %s, protocol: %s, extensions: %s", name,
                    state, group, room, protocol, extensions);
        } else {
            Log.info(LogLevel.Minimal, "No client named % was found", name);
        }
    }

    //----- Server event handlers -----
    @EventHandler(eventId = TcpServer.EVENT_STARTED)
    private void eventStarted(int port) {
        Log.info(LogLevel.Minimal, "Started server on port %d", port);
    }

    @EventHandler(eventId = TcpServer.EVENT_START_FAILED)
    private void eventStartFailed(int port, String reason) {
        Log.error(LogLevel.Minimal, "Failed to start server on port %d (%s)", port, reason);
    }

    @EventHandler(eventId = TcpServer.EVENT_STOPPED)
    private void eventStopped() {
        Log.info(LogLevel.Minimal, "Server stopped");
    }

    @EventHandler(eventId = TcpServer.EVENT_CLIENT_CONNECTED)
    private void eventClientConnected(TcpServer.Client client, String host) {
        Log.info(LogLevel.Normal, "Client connected from %s", host);

        playerManager.createSession(client);
    }

    @EventHandler(eventId = TcpServer.EVENT_CLIENT_DISCONNECTED)
    private void eventClientDisconnected(TcpServer.Client client) {
        Player player = playerManager.get(client);

        Log.info(LogLevel.Normal, "Client %s disconnected", player.getName());

        playerManager.endSession(client);
    }

    @EventHandler(eventId = TcpServer.EVENT_PACKET_RECEIVED)
    private void eventPacketReceived(TcpServer.Client client, String packet) {
        Player player = playerManager.get(client);

        Log.debug("Received packet from %s: %s", player.getName(), packet);

        player.getProtocol().handlePacket(packet);
    }

    @EventHandler(eventId = TcpServer.EVENT_SEND_FAILED)
    private void eventSendFailed(TcpServer.Client client, String reason) {
        Player player = playerManager.get(client);

        Log.error(LogLevel.Minimal, "Failed to send packet to %s: %s", player.getName(), reason);
    }

    //----- Getters -----
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public RoomManager getRoomManager() {
        return roomManager;
    }

    public MatchMaker getMatchMaker() {
        return matchMaker;
    }

    public Challenger getChallenger() {
        return challenger;
    }

    public TcpServer getServer() {
        return server;
    }

    //----- Entry point -----
    public static void main(String[] args) {
        INSTANCE.start();
    }
}
