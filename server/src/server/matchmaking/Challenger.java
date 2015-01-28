package server.matchmaking;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import server.Main;
import server.player.Player;
import server.player.PlayerManager;
import server.player.PlayerState;
import findfour.shared.logging.Log;
import findfour.shared.logging.LogLevel;

public final class Challenger {
    private final PlayerManager playerManager;
    private final Map<String, List<String>> pendingChallenges;

    public Challenger() {
        this.playerManager = Main.INSTANCE.getPlayerManager();
        this.pendingChallenges = new HashMap<String, List<String>>();
    }

    public synchronized void challenge(Player challenger, String name) {
        Player challengee = playerManager.getIfExists(name);

        if (challengee == null) {
            challenger.getProtocol().sendCannotChallenge("Opponent does not exist");
        } else if (challengee.getState() != PlayerState.InLobby) {
            challenger.getProtocol().sendCannotChallenge("Opponent must be in the lobby state");
        } else if (!challengee.getProtocol().supportsChallenging()) {
            challenger.getProtocol().sendCannotChallenge("Opponent does not support challenging");
        } else if (!hasChallenged(challenger.getName(), challengee.getName())) {
            // This check prevents spamming the challengee with challenge requests if one has
            // already been send, but not yet has received an answer.
            completeChallengeRequest(challenger, challengee);
        }
    }

    public synchronized void handleChallengeResponse(Player challengee, String name, boolean resp) {
        Player challenger = playerManager.get(name);

        if (challenger != null) {
            if (hasChallenged(name, challengee.getName())) {
                if (resp) {
                    if (challenger.getState() == PlayerState.InLobby) {
                        Log.info(LogLevel.Normal, "%s has accepted the challenge from %s",
                                challengee.getName(), name);

                        Main.INSTANCE.getRoomManager().startGame(challenger, challengee);
                    } else {
                        // Challenger is no longer in the required state.
                        challengee.getProtocol().sendChallengeFailed(
                                "Challenger is not in the lobby state");
                    }
                } else {
                    Log.info(LogLevel.Normal, "%s has declined the challenge from %s",
                            challengee.getName(), name);
                }

                removeChallengeRequest(name, challengee.getName());
            } else {
                // Challenger has not send a challenge request.
                challengee.getProtocol().sendChallengeFailed(
                        "The challenger has not challenged you");
            }
        } else {
            // Send invalid challenger name error.
            challengee.getProtocol().sendChallengeFailed("Unkown challenger name");
        }
    }

    public synchronized void handlePlayerDisconnect(Player player) {
        String name = player.getName();

        // Cancel all pending challenges send by the player.
        if (pendingChallenges.containsKey(name)) {
            pendingChallenges.remove(name);
        }

        // Cancel all pending challenges send to this player.
        for (Map.Entry<String, List<String>> entry : pendingChallenges.entrySet()) {
            entry.getValue().remove(name);
        }
    }

    private void completeChallengeRequest(Player challenger, Player challengee) {
        String challengerName = challenger.getName();
        String challengeeName = challengee.getName();

        if (!pendingChallenges.containsKey(challengerName)) {
            pendingChallenges.put(challengerName, new LinkedList<String>());
        }

        pendingChallenges.get(challengerName).add(challengeeName);

        challengee.getProtocol().sendChallengeNotify(challengerName);

        Log.info(LogLevel.Normal, "%s has challenged %s", challengerName, challengeeName);
    }

    private boolean hasChallenged(String challenger, String challengee) {
        if (!pendingChallenges.containsKey(challenger)) {
            return false;
        }

        return pendingChallenges.get(challenger).contains(challengee);
    }

    private void removeChallengeRequest(String challenger, String challengee) {
        if (pendingChallenges.containsKey(challenger)) {
            pendingChallenges.get(challenger).remove(challengee);
        }
    }
}
