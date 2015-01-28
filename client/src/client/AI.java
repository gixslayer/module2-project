package client;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import findfour.shared.game.Board;
import findfour.shared.game.Disc;

/**
 * Created by joran on 27-1-15.
 */
public class AI {
    private ClientController clientController;
    private State currentState;
    private int lookahead;

    public AI(ClientController c) {
        this.clientController = c;
        this.lookahead = 6;
        //TODO mak static
        currentState = new State(true);
    }

    public void doMove(Boolean myTurn) {
        currentState = new State(myTurn);

        createDecisionTree(currentState);
        calculateChanses(currentState);

        ArrayList<State> bestmoves = getBestMove();
        int randomint = new Random().nextInt(bestmoves.size());

        clientController.tryMove(bestmoves.get(randomint).col, clientController.getClientName());
    }

    public int getHint() {
        currentState = new State(true);

        createDecisionTree(currentState);
        calculateChanses(currentState);

        ArrayList<State> bestmoves = getBestMove();
        for (State t : bestmoves) {
            System.out.println(t.getV());
        }

        int randomint = new Random().nextInt(bestmoves.size());
        return bestmoves.get(randomint).col;
    }

    public ArrayList<State> getBestMove() {
        ArrayList<State> temp = new ArrayList<State>();
        for (State t : currentState.getNextStep()) {
            if (temp.size() < 1) {
                temp.add(t);
            } else if (temp.get(0).v < t.v) {
                temp.clear();
                temp.add(t);
            } else if (temp.get(0).v == t.v) {
                temp.add(t);
            }
        }
        return temp;
    }

    public void createDecisionTree(State state) {
        currentState.syncTo(clientController.getBoard());
        Disc disc;
        Vector<State> result = new Vector<State>();
        for (int i = 0; i < State.getColumns(); i++) {
            State temp = new State(!state.isMyTurn());
            temp.syncTo(state);
            if (temp.isMyTurn()) {
                disc = Disc.Red;
            } else {
                disc = Disc.Yellow;
            }
            if (temp.isMoveValid(i, disc)) {
                temp.makeMove(i, disc);
                temp.depth = state.depth + 1;
                temp.col = i;
                result.add(temp);
                if (temp.depth < lookahead) {
                    createDecisionTree(temp);
                }
            }

        }
        state.nextStep = result;
    }

    public void createDecisionTree() {
        createDecisionTree(currentState);
    }

    public int calculateChance(State state) {
        int result = 0;
        if (state.hasWinner()) {
            if (state.getWinner() == Disc.Red) {
                return 110;
            } else {
                return 100;
            }
        }
        return result;
    }

    public int calculateChanses(State state) {
        int result = 0;
        for (State s : state.getNextStep()) {
            state.v = calculateChance(s);
            state.v = state.v + calculateChanses(s) / (s.depth * 3);
            result += state.v;
        }
        return result;
    }

    private class State extends Board {
        public int v;
        public int depth;
        public int col;
        public boolean out;
        public Vector<State> nextStep;
        private boolean myTurn;

        State(Boolean myturn) {
            this.myTurn = myturn;
            col = 0;
            v = 0;
            depth = 0;
            nextStep = new Vector<State>();
            out = false;
        }

        public boolean isMyTurn() {
            return myTurn;
        }

        public Vector<State> getNextStep() {
            return nextStep;
        }

        @SuppressWarnings("unused")
        public void setChance(int chance) {
            v = chance;
        }

        public int getV() {
            return v;
        }
    }

    //TODO remove test main
    public static void main(String[] args) {
        ClientController c = new ClientController();
        AI ai = new AI(c);
        c.resetBoard();
        ai.createDecisionTree();
        ai.calculateChanses(ai.currentState);
        for (State t : ai.currentState.getNextStep()) {
            System.out.println(t.getV());
        }
    }

}
