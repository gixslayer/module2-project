package server.rooms;

import server.Main;
import server.player.Player;
import findfour.shared.game.Disc;
import findfour.shared.game.GameState;

public final class GameRoom extends Room {
    private final String roomName;
    private final GameState gameState;
    private final Player playerRed;
    private final Player playerYellow;
    private Player currentTurn;

    public GameRoom(String name, Player playerA, Player playerB) {
        this.roomName = name;
        this.gameState = new GameState();
        this.playerRed = playerA;
        this.playerYellow = playerB;
        this.currentTurn = GameState.STARTING_COLOR == Disc.Red ? playerRed : playerYellow;
    }

    public void begin() {
        playerRed.setColor(Disc.Red);
        playerYellow.setColor(Disc.Yellow);

        playerRed.getProtocol().sendStartGame(playerYellow.getName());
        playerYellow.getProtocol().sendStartGame(playerRed.getName());

        String startingPlayer = currentTurn.getName();

        // Broadcast to all players in the room. Spectators could be added to this list.
        for (Player player : getPlayers()) {
            player.getProtocol().sendRequestMove(startingPlayer);
        }
    }

    public synchronized void handleMove(Player player, int column) {
        if (currentTurn == player) {
            if (gameState.isMoveValid(column, player.getColor())) {
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
            } else {
                player.getProtocol().sendInvalidMove();
            }
        } else {
            player.getProtocol().sendNotYourMove();
        }
    }

    public String getName() {
        return roomName;
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
