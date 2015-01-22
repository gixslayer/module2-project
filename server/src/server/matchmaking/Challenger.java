package server.matchmaking;

import server.Main;
import server.player.Player;
import server.player.PlayerState;

// TODO: The current challenging system is very ugly and probably broken, improve it.
// NOTE: These get calls on PlayerManager are bound to crash on high traffic servers as a client
// could always disconnect while this method is running. Use something like getIfExists which would
// return null instead of throw an exception.
public final class Challenger {
    public boolean challenge(Player challenger, String name) {
        if (!Main.INSTANCE.getPlayerManager().hasSession(name)) {
            return false;
        }

        Player challengee = Main.INSTANCE.getPlayerManager().get(name);

        if (challengee.getState() != PlayerState.InLobby) {
            return false;
        } else if (!challengee.getProtocol().supportsChallenging()) {
            return false;
        } else if (challengee.hasChallenger(challenger)) {
            return true;
        }

        challengee.addChallenger(challenger);
        challengee.getProtocol().sendChallengeNotify(challenger.getName());

        return true;
    }

    public boolean handleChallengeResponse(Player challengee, String name, boolean answer) {
        if (!Main.INSTANCE.getPlayerManager().hasSession(name)) {
            return false;
        }

        Player challenger = Main.INSTANCE.getPlayerManager().get(name);

        if (challengee.hasChallenger(challenger)) {
            if (answer) {
                if (challenger.getState() == PlayerState.InLobby) {
                    challengee.removeChallenger(challenger);

                    Main.INSTANCE.getRoomManager().startGame(challenger, challengee);

                    return true;
                } else {
                    challengee.removeChallenger(challenger);
                    return false;
                }
            } else {
                challengee.removeChallenger(challenger);
                return true;
            }
        } else {
            return false;
        }
    }
}
