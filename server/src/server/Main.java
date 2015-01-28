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

/**
 * The main application class.
 * @author ciske
 *
 */
public final class Main {
    /**
     * The Main instance, which can be statically referred to from other classes.
     */
    public static final Main INSTANCE = new Main();
    /**
     * The delimiter used to parse commands.
     */
    private static final char COMMAND_DELIMITER = ' ';

    private final TcpServer server;
    private final PlayerManager playerManager;
    private final RoomManager roomManager;
    private final MatchMaker matchMaker;
    private final Challenger challenger;
    private final BufferedReader input;
    private final CommandInvoker commandInvoker;
    private boolean keepRunning;

    /**
     * Create a new instance of the Main class.
     */
    private Main() {
        this.server = new TcpServer();
        this.playerManager = new PlayerManager();
        this.roomManager = new RoomManager();
        this.matchMaker = new MatchMaker();
        this.challenger = new Challenger(playerManager);
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.commandInvoker = new CommandInvoker();
        this.keepRunning = true;
    }

    /**
     * Start the server application.
     */
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

    /**
     * Initialize the server application.
     */
    private void initialize() {
        Log.setLogLevel(LogLevel.Verbose);
        Log.setDebugEnabled(true);
        Log.registerListener(new ConsoleListener());

        server.registerEventHandlers(this);
        commandInvoker.registerHandlers(this);
    }

    /**
     * Perform cleanup required when the server application exits.
     */
    //@ ensures server.isRunning() == false;
    private void cleanup() {
        if (server.isRunning()) {
            server.stop();
        }
    }

    /**
     * Handle a command entered by the user.
     * @param rawCommand The command to handle.
     */
    //@ requires rawCommand != null;
    private void handleCommand(String rawCommand) {
        String command = StringUtils.extractCommand(rawCommand, COMMAND_DELIMITER);
        String[] args = StringUtils.extractArgs(rawCommand, COMMAND_DELIMITER, true);

        commandInvoker.invoke(command, args);
    }

    //----- Command handlers -----
    /**
     * Handles the start command used to start listening for clients.
     * @param port The port to listen on which must be between 0 and 65536.
     */
    //@ requires port >= 0 && port <= 65536;
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

    /**
     * Handles the stop command used to stop listening for clients and disconnect all connected
     * clients.
     */
    @CommandHandler
    private void cmdStop() {
        if (server.isRunning()) {
            server.stop();
        } else {
            Log.info(LogLevel.Minimal, "Server wasn't running");
        }
    }

    /**
     * Handles the quit command used to exit the server application.
     */
    //@ ensures keepRunning == false;
    @CommandHandler
    private void cmdQuit() {
        keepRunning = false;
    }

    /**
     * Handles the setLogLevel command used to set the current log level.
     * @param level The log level to use.
     */
    //@ requires level != null;
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

    /**
     * Handles the debugMode command used to enabled/disable the debug mode.
     * @param value The boolean value that indicates if debug mode will be enabled.
     */
    //@ ensures Log.isDebugEnabled() == value;
    @CommandHandler
    private void cmdDebugMode(boolean value) {
        Log.setDebugEnabled(value);

        Log.info(LogLevel.Minimal, "%s debug mode", value ? "Enabled" : "Disabled");
    }

    /**
     * Handles the listClients command used to list all connected clients and some basic information
     * about them.
     */
    @CommandHandler
    private void cmdListClients() {
        for (Player player : playerManager.getAll()) {
            Log.info(LogLevel.Minimal, "%s, state: %s, group: %s, protocol: %s", player.getName(),
                    player.getState(), player.getGroup(), player.getProtocol().getName());
        }
    }

    /**
     * Handles the listGames command used to list all active games.
     */
    @CommandHandler
    private void cmdListGames() {
        for (GameRoom game : roomManager.getGameRooms()) {
            Log.info(LogLevel.Minimal, "%s (%d spectators)", game.getName(),
                    game.getSpectatorCount());
        }
    }

    /**
     * Handles the clientInfo command used to list information about a specific client.
     * @param name The name of the client to list information for
     */
    //@ requires name != null;
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
    /**
     * Handles the started event raised by the server once the server successfully starts listening
     * on a port.
     * @param port The port on which the server started.
     */
    //@ requires port >= 0 && port <= 65536;
    @EventHandler(eventId = TcpServer.EVENT_STARTED)
    private void eventStarted(int port) {
        Log.info(LogLevel.Minimal, "Started server on port %d", port);
    }

    /**
     * Handles the startFailed event raised by the server once the server fails to start listening
     * on a port.
     * @param port The port on which the server failed to listen
     * @param reason The reason why the server failed to listen
     */
    //@ requires port >= 0 && port <= 65536;
    //@ requires reason != null;
    @EventHandler(eventId = TcpServer.EVENT_START_FAILED)
    private void eventStartFailed(int port, String reason) {
        Log.error(LogLevel.Minimal, "Failed to start server on port %d (%s)", port, reason);
    }

    /**
     * Handles the stopped event raised by the server once the server stops listening and has
     * disconnected all connected clients.
     */
    @EventHandler(eventId = TcpServer.EVENT_STOPPED)
    private void eventStopped() {
        Log.info(LogLevel.Minimal, "Server stopped");
    }

    /**
     * Handles the clientConnected event raised by the server once a client connects to the server.
     * @param client The client instance that just connected.
     * @param host The remote host from which the client connected.
     */
    //@ requires client != null;
    //@ requires host != null;
    //@ ensures playerManager.hasSession(client) == true;
    @EventHandler(eventId = TcpServer.EVENT_CLIENT_CONNECTED)
    private void eventClientConnected(TcpServer.Client client, String host) {
        Log.info(LogLevel.Normal, "Client connected from %s", host);

        playerManager.createSession(client);
    }

    /**
     * Handles the clientDisconnected event raised by the server once a client disconnects from the
     * server.
     * @param client The client instance that just disconnected
     */
    //@ requires client != null;
    //@ ensures playerManager.hasSession(client) == false;
    @EventHandler(eventId = TcpServer.EVENT_CLIENT_DISCONNECTED)
    private void eventClientDisconnected(TcpServer.Client client) {
        Player player = playerManager.get(client);

        Log.info(LogLevel.Normal, "Client %s disconnected", player.getName());

        playerManager.endSession(client);
    }

    /**
     * Handles the packetReceived event raised by the server once a packet is received from a
     * client.
     * @param client The client which send the packet
     * @param packet The packet data that was received
     */
    //@ requires client != null;
    //@ requires packet != null;
    //@ requires playerManager.hasSession(client) == true;
    @EventHandler(eventId = TcpServer.EVENT_PACKET_RECEIVED)
    private void eventPacketReceived(TcpServer.Client client, String packet) {
        Player player = playerManager.get(client);

        Log.debug("Received packet from %s: %s", player.getName(), packet);

        player.getProtocol().handlePacket(packet);
    }

    /**
     * Handles the sendFailed event raised by the server once a packet could not be send to a
     * client.
     * @param client The client to which the packet could not be send
     * @param reason The reason why the packet could not be send
     */
    //@ requires client != null;
    //@ requires reason != null;
    //@ requires playerManager.hasSession(client) == true;
    @EventHandler(eventId = TcpServer.EVENT_SEND_FAILED)
    private void eventSendFailed(TcpServer.Client client, String reason) {
        Player player = playerManager.get(client);

        Log.error(LogLevel.Minimal, "Failed to send packet to %s: %s", player.getName(), reason);
    }

    //----- Getters -----
    /**
     * Returns the PlayerManager instance.
     */
    /*@ pure */
    //@ ensures \result != null;
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Returns the RoomManager instance.
     */
    /*@ pure */
    //@ ensures \result != null;
    public RoomManager getRoomManager() {
        return roomManager;
    }

    /**
     * Returns the MatchMaker instance.
     */
    /*@ pure */
    //@ ensures \result != null;
    public MatchMaker getMatchMaker() {
        return matchMaker;
    }

    /**
     * Returns the Challenger instance.
     */
    /*@ pure */
    //@ ensures \result != null;
    public Challenger getChallenger() {
        return challenger;
    }

    /**
     * Returns the TcpServer instance.
     */
    /*@ pure */
    //@ ensures \result != null;
    public TcpServer getServer() {
        return server;
    }

    //----- Entry point -----
    /**
     * The entry point of the server application.
     * @param args ignored
     */
    public static void main(String[] args) {
        INSTANCE.start();
    }
}
