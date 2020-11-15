package games.BlackJack;

import java.util.ArrayList;

import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types.ACTIONS;
import tools.Types;

public class StateObserverBlackJack extends ObserverBase implements StateObsNondeterministic {

    // First version only features 1 Player vs the Dealer, N Players vs Dealer will
    // be added later.

    private static final long serialVersionUID = 1L;
    private Player p1;
    private Dealer dealer;
    private ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>();
    private Player currentPlayer;
    private boolean dealersTurn = false;
    private boolean isNextActionDeterministic = false;
    private ArrayList<Integer> availableRandoms = new ArrayList<Integer>();

    public StateObserverBlackJack() {
        // defaultState
        // adding dealer and player/s
        dealer = new Dealer();
        p1 = new Player("p1");
        currentPlayer = p1;
    }

    enum BlackJackActionDet {
        BET1(0), BET5(1), BET10(2), BET25(3), BET50(4), BET100(5), HIT(6), STAND(7), DOUBLEDOWN(8), SPLIT(9),
        INSURANCE(10);

        private int action;

        private BlackJackActionDet(int action) {
            this.action = action;
        }

        public int getAction() {
            return this.action;
        }
    }

    enum BlackJackActionNonDet {
        CHECKFORBLACKJACK(0), CHECKFORBUST(1), DETECTPLAYERSTURN(2);

        private int action;

        private BlackJackActionNonDet(int action) {
            this.action = action;
        }

        public int getAction() {
            return this.action;
        }
    }

    @Override
    public StateObservation clearedCopy() {
        return null;
    }

    @Override
    public boolean isGameOver() {
        return p1.getChips() == 0;
    }

    @Override
    public boolean isDeterministicGame() {
        return false;
    }

    @Override
    public boolean isFinalRewardGame() {
        return false;
    }

    @Override
    public boolean isLegalState() {
        return true;
    }

    @Override
    public boolean stopInspectOnGameOver() {// ????
        return false;
    }

    @Override
    public String stringDescr() {
        return null;
    }

    @Override
    public String stringActionDescr(ACTIONS act) {
        return null;
    }

    @Override
    public double getGameScore(StateObservation referringState) {
        return 0;
    }

    @Override
    public double getGameScore(int player) {
        return 0;
    }

    @Override
    public ScoreTuple getGameScoreTuple() {
        return null;
    }

    @Override
    public double getReward(StateObservation referringState, boolean rewardIsGameScore) {
        return 0;
    }

    @Override
    public double getReward(int player, boolean rewardIsGameScore) {
        return 0;
    }

    @Override
    public ScoreTuple getRewardTuple(boolean rewardIsGameScore) {
        return null;
    }

    @Override
    public ScoreTuple getStepRewardTuple() {
        return null;
    }

    @Override
    public double getMinGameScore() {
        return 0;
    }

    @Override
    public double getMaxGameScore() {
        return 1000000;
    }

    @Override
    public int getMinEpisodeLength() {
        return 0;
    }

    @Override
    public int getMoveCounter() {
        return 0;
    }

    @Override
    public void resetMoveCounter() {

    }

    @Override
    public String getName() {
        return "BlackJack";
    }

    @Override
    public void advance(ACTIONS action) {
        if (isNextActionDeterministic()) {
            advanceDeterministic(action);
        }
        while (!isNextActionDeterministic()) {
            advanceNondeterministic();
        }
    }

    @Override
    public StateObservation getPrecedingAfterstate() {
        return null;
    }

    @Override
    public ArrayList<ACTIONS> getAllAvailableActions() {
        ArrayList<ACTIONS> allActions = new ArrayList<ACTIONS>();
        for (BlackJackActionDet a : BlackJackActionDet.values()) {
            allActions.add(ACTIONS.fromInt(a.getAction()));
        }
        return allActions;

    }

    @Override
    public ArrayList<ACTIONS> getAvailableActions() {
        return availableActions;
    }

    @Override
    public int getNumAvailableActions() {
        return availableActions.size();
    }

    @Override
    public void setAvailableActions() {
        availableActions.clear();

        if (!currentPlayer.hasHand()) {
            // sets the available betting options, u always bet before get dealt a hand.
            // Checks
            // the highest possible betamount first. Actions are mapped to Enums.
            int availableBetCount = 0;
            if (currentPlayer.getChips() >= 100) {
                availableBetCount = 6;
            } else if (currentPlayer.getChips() >= 50) {
                availableBetCount = 5;
            } else if (currentPlayer.getChips() >= 25) {
                availableBetCount = 4;
            } else if (currentPlayer.getChips() >= 10) {
                availableBetCount = 3;
            } else if (currentPlayer.getChips() >= 5) {
                availableBetCount = 2;
            } else if (currentPlayer.getChips() >= 1) {
                availableBetCount = 1;
            }
            try {
                setAvailableBettingActions(availableBetCount);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            // enters after Player has placed his bet
            // assuming this is only entered if u are not bust and u got no BlackJack nor 21
            // this should be detected somewher else

            // Stand - Player wants no more cards for this hand
            availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.getAction()));
            // Hit - Player wants one more card for this Hand
            availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.HIT.getAction()));
            // Split - Player wants to split a pair -
            // Condition: (Handsize == 2) and both cards are from
            // the same Rank e.g. 8 8, player has enough chips to do so
            ArrayList<Card> playersHand = currentPlayer.getActiveHand();
            if (playersHand.size() == 2 && playersHand.get(0).rank.equals(playersHand.get(1).rank)
                    && currentPlayer.betOnActiveHand() <= currentPlayer.getChips()) {
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.SPLIT.getAction()));
            }
            // Doubledown - Player gets dealt exactly one more card and doubles the
            // betamount for this hand
            // Condition: Handsize == 2, player has enough chips to do so
            if (playersHand.size() == 2 && currentPlayer.betOnActiveHand() <= currentPlayer.getChips()) {
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.DOUBLEDOWN.getAction()));
            }
            // TODO: Insurance
        }

    }

    // param availableBets: represents how many bets are Available from enum
    // BlackJackActionDet from low to high
    public void setAvailableBettingActions(int availableBets) throws Exception {
        if (availableBets == 0 || availableBets > BlackJackActionDet.values().length)
            throw new Exception("");
        for (int i = 0; i < availableBets; i++) {
            availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.values()[i].getAction()));
        }
    }

    @Override
    public ACTIONS getAction(int i) {
        return null;
    }

    @Override
    public void storeBestActionInfo(ACTIONS actBest, double[] vtable) {

    }

    @Override
    public int getPlayer() {
        return 0;
    }

    @Override
    public int getCreatingPlayer() {
        return 0;
    }

    @Override
    public int getNumPlayers() {
        return 0;
    }

    @Override
    public void advanceDeterministic(ACTIONS action) {

        BlackJackActionDet a = BlackJackActionDet.values()[action.toInt()];
        switch (a) {
        case BET1:
            currentPlayer.bet(1);
            break;
        case BET5:
            currentPlayer.bet(5);
            break;
        case BET10:
            currentPlayer.bet(10);
            break;
        case BET25:
            currentPlayer.bet(25);
            break;
        case BET50:
            currentPlayer.bet(50);
            break;
        case BET100:
            currentPlayer.bet(100);
            break;
        case STAND:
            // assign next player, currently there is only one so its the dealers turn
            dealersTurn = true;
            currentPlayer = null;
            break;
        case HIT:
            // The player wants another card, this should be a nondetermenistic Event
            /**
             * Frage merken: Falls jede moeglich Auszuteilende Karte ein Available Random
             * ist und man diese Up to Date halten wuerde, haetten die Agenten Ueberblick
             * darueber welche Karten noch im Deck vorhanden sind oder? Das waere genau dass
             * was man beim Kartenzaehlen machen wuerde.
             * 
             */
            // isNextActionDeterministic = true;
            // nondeterministicAdvance Deal card to player??
            // check if he is bust
            break;
        case DOUBLEDOWN:
            // double his bet
            currentPlayer.bet(currentPlayer.betOnActiveHand());
            // he will get exactly one more card dealt
            // isNextActionDeterministic = true;
            // nondeterministicAdvance Deal card to player??
            break;
        case SPLIT:
            currentPlayer.splitHand();
            // Der Spieler Spielt nun zwei Haende, seine erste Hand braucht eine weitere
            // Karte -> Nondeterministisches Event
            // nachdem er seine 1. Hand fertig gespielt hat braucht seine 2. Hand eine Karte
            // -> Nondeterministisches Event
            // Wann ist eine Hand fertig? Wenn ein Blackjack oder 21 erreicht wurde, bei
            // Stand und einem Bust der bei jedem
            // DOUBLEDOWN und HIT auftreten kann.
            // Eine Gesplittete Hand erneut zu splitten ist vorerst nicht moeglich und in je
            // nach Regeln auch verboten
            // Wenn Asse gesplittet werden kann je nach Hausregeln nur eine Weitere Karte
            // pro Hand genommen werden
            break;
        // case INSURANCE: break;
        }

    }

    @Override
    public void advanceNondeterministic() {

    }

    @Override
    public void advanceNondeterministic(ACTIONS action) {

    }

    @Override
    public boolean isNextActionDeterministic() {
        return isNextActionDeterministic;

    }

    @Override
    public ACTIONS getNextNondeterministicAction() {
        if (availableRandoms.isEmpty()) {
            return null;
        }
        return ACTIONS.fromInt(availableRandoms.remove(0));
    }

    @Override
    public ArrayList<ACTIONS> getAvailableRandoms() {
        return null;
    }

    @Override
    public int getNumAvailableRandoms() {
        // 52cards*6decks
        return 52 * 6;
    }

    @Override
    public double getProbability(ACTIONS action) {
        return 0;
    }

    @Override
    public StateObsNondeterministic copy() {
        return null;
    }

}
