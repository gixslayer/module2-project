package server.rooms;

import server.Main;
import server.player.Player;
import findfour.shared.game.Disc;
import findfour.shared.game.GameState;

public final class GameRoom extends Room {
    private final GameState gameState;
    private final Player playerRed;
    private final Player playerYellow;
    private Player currentTurn;

    public GameRoom(String name, Player playerA, Player playerB) {
        super(name);

        this.gameState = new GameState();
        this.playerRed = playerA;
        this.playerYellow = playerB;
    }

    public void begin() {
        playerRed.setColor(Disc.Red);
        playerYellow.setColor(Disc.Yellow);
        currentTurn = GameState.STARTING_COLOR == Disc.Red ? playerRed : playerYellow;

        String startingPlayer = currentTurn.getName();
        String otherPlayer = getOpponent(currentTurn).getName();

        playerRed.getProtocol().sendStartGame(startingPlayer, otherPlayer);
        playerYellow.getProtocol().sendStartGame(startingPlayer, otherPlayer);

        // Broadcast to all players in the room. Spectators could be added to this list.
        for (Player player : getPlayers()) {
            player.getProtocol().sendRequestMove(startingPlayer);
        }
    }

    public synchronized void handleMove(Player player, int column) {
        if (currentTurn != player) {
            player.getProtocol().sendNotYourMove();
        } else if (!gameState.isMoveValid(column, player.getColor())) {
            player.getProtocol().sendInvalidMove();
        } else {
            gameState.makeMove(column, player.getColor());

            String playerName = player.getName();
            // Broadcast the move to all players in the room.
            for (Player player2 : getPlayers()) {
                player2.getProtocol().sendDoneMove(playerName, column);
            }

            if (gameState.isGameOver()) {
                handleGameOver();
            } else {
                beginNextTurn();
            }
        }
    }

    @Override
    public synchronized void onPlayerDisconnect(Player player) {
        // Call the base method first to remove the player from the players list. This prevents
        // possibly trying to send data to that disconnected player.
        super.onPlayerDisconnect(player);

        if (isPlayer(player)) {
            // One of the players disconnected. End the game.
            Player opponent = getOpponent(player);
            String opponentName = opponent.getName();

            // Notify the opponent that his opponent has disconnected.
            opponent.getProtocol().sendOpponentDisconnected();

            // Notify everyone in the game that there is a winner.
            for (Player player2 : getPlayers()) {
                // TODO: Spectators should really receive a 'player x disconnected' message along
                // with the winner.
                player2.getProtocol().sendGameWon(opponentName);
            }

            Main.INSTANCE.getRoomManager().endGame(this);
        }
    }

    public int getSpectatorCount() {
        return getPlayers().size() - 2;
    }

    private boolean isPlayer(Player player) {
        return player == playerRed || player == playerYellow;
    }

    private Player getOpponent(Player player) {
        // NOTE: Assumes isPlayer(player) == true.
        return player == playerRed ? playerYellow : playerRed;
    }

    private void handleGameOver() {
        Disc winningColor = gameState.getWinner();

        if (winningColor == Disc.None) {
            // Game has no winner, but has ended, thus it is a draw.
            for (Player player : getPlayers()) {
                player.getProtocol().sendGameDraw();
            }
        } else {
            String winner = winningColor == Disc.Red ? playerRed.getName() : playerYellow.getName();

            for (Player player : getPlayers()) {
                player.getProtocol().sendGameWon(winner);
            }
        }

        Main.INSTANCE.getRoomManager().endGame(this);
    }

    private void beginNextTurn() {
        Disc currentColor = gameState.getCurrentTurn();

        currentTurn = currentColor == Disc.Red ? playerRed : playerYellow;
        String playerName = currentTurn.getName();

        for (Player player : getPlayers()) {
            player.getProtocol().sendRequestMove(playerName);
        }
    }
}
