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
    private int NUM_PLAYERS = 2;
    private Player p1;
    private ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>();
    private Player currentPlayer;
    private boolean isNextActionDeterministic = false;
    private ArrayList<Integer> availableRandoms = new ArrayList<Integer>();
    private Deck deck = new Deck();
    private Player dealer;
    private Player players[] = new Player[2];
    private int playersTurn;

    public StateObserverBlackJack() {
        // defaultState
        // adding dealer and player/s
        dealer = new Player("dealer");
        p1 = new Player("p1");
        players[0] = p1;
        players[1] = dealer;
        playersTurn = 0;
        currentPlayer = getCurrentPlayer();
    }

    public StateObserverBlackJack(StateObserverBlackJack other) {
        super(other);
        this.playersTurn = other.playersTurn;
        this.dealer = new Player(other.dealer);
        this.p1 = new Player(other.p1);
        this.availableRandoms = new ArrayList<>(other.availableRandoms);
        this.availableActions = new ArrayList<>(other.availableActions);
        this.isNextActionDeterministic = other.isNextActionDeterministic;
        this.currentPlayer = getCurrentPlayer();
        this.players[0] = p1;
        this.players[1] = dealer;
    }

    enum BlackJackActionDet {
        BET1(0), BET5(1), BET10(2), BET25(3), BET50(4), BET100(5), HIT(6), STAND(7), DOUBLEDOWN(8), SPLIT(9),
        SURRENDER(10), INSURANCE(11);

        private int action;

        private BlackJackActionDet(int action) {
            this.action = action;
        }

        public int getAction() {
            return this.action;
        }
    }

    enum BlackJackActionNonDet {
        DEALCARD(0), DEALERPLAYS(1), PAYPLAYERS(2);

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
        return "null";
    }

    @Override
    public String stringActionDescr(ACTIONS act) {
        return "null";
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
    public StateObservation precedingAfterstate() {
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
        } else if (!currentPlayer.getActiveHand().isHandFinished()) {
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
            ArrayList<Card> playersHand = currentPlayer.getActiveHand().getCards();
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
        } else { // The hand is finished, the player can only stand/passToNext
            availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.getAction()));
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
        return playersTurn;
    }

    public void setPlayer(int p) {
        playersTurn = p;
    }

    @Override
    public int getCreatingPlayer() {
        return 0;
    }

    @Override
    public int getNumPlayers() {
        return 2;
    }

    @Override
    public void advanceDeterministic(ACTIONS action) {

        BlackJackActionDet a = BlackJackActionDet.values()[action.toInt()];
        isNextActionDeterministic = true;
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
                break;
            case HIT:
                // The player wants another card, this should be a nondetermenistic Event
                // isNextActionDeterministic = false;
                // nondeterministicAdvance Deal card to player??
                // check if he is bust
                break;
            case DOUBLEDOWN:
                // double his bet
                currentPlayer.bet(currentPlayer.betOnActiveHand());
                // he will get exactly one more card dealt
                // isNextActionDeterministic = false;
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
        switch (a) {
            case BET1:
            case BET5:
            case BET10:
            case BET25:
            case BET50:
            case BET100:
            case STAND:
                isNextActionDeterministic = !isNextPlayerDealer();
                passToNextPlayer();
                break;
            case HIT:
            case DOUBLEDOWN:
            case SPLIT:
                isNextActionDeterministic = false;
                break;

        }

    }

    public boolean isDealPhase() {
        // dealers handsize < 2 ?
        return dealer.getActiveHand().size() < 2;
    }

    @Override
    public void advanceNondeterministic() {
        advanceDeterministic(getNextNondeterministicAction());
    }

    @Override
    public void advanceNondeterministic(ACTIONS action) {
        if (isNextActionDeterministic) {
            throw new RuntimeException("Next action should be deterministic");
        }
        BlackJackActionNonDet a = BlackJackActionNonDet.values()[action.toInt()];
        switch (a) {
            case DEALCARD:
                // NonDeterministic part
                currentPlayer.addCardToActiveHand(deck.draw());

                // Consequenses of it
                if (isDealPhase()) {
                    passToNextPlayer();
                    isNextActionDeterministic = false;
                } else if (!currentPlayer.equals(dealer)) {
                    Hand currentHand = currentPlayer.getActiveHand();
                    BlackJackActionDet lastAction = BlackJackActionDet.values()[getLastMove()];
                    switch (lastAction) {
                        case DOUBLEDOWN: // Double down is the only case, where the hand ends for sure after
                            // an nondeterministic event. After a Hit the hand will only end, if he got a
                            // blackjack, 21, or handvalue > 21
                            currentHand.setHandFinished();
                            break;
                    }
                    if (currentHand.isHandFinished()) { // is handfinished checks for blackjack, handvalue > 20, or if
                                                        // the
                                                        // hand is finished by decision
                        // check for more hands
                        if (currentPlayer.setNextHandActive() != null) {
                            // The player has more hands this will only occure if a hand is split
                            // so the handsize of it is 1 -> the hand needs to get dealt one more card so
                            // the next advance is nonDet
                            isNextActionDeterministic = false;
                        } else {
                            // The players hand is finished and he has no more Hands
                            isNextActionDeterministic = true;
                            passToNextPlayer();
                        }
                    } else {
                        isNextActionDeterministic = true;
                    }
                }

                break;

            case DEALERPLAYS:
                // reveal unknown card ??
                while (currentPlayer.getActiveHand().getHandValue() < 17) {
                    // The dealer will always hit under 17 and will always stay on 17 or higher,
                    // even if the opponent got 18, dealer got 17 and there is only this one
                    // opponent
                    currentPlayer.activeHand.addCard(deck.draw());
                }

                // next action will be nondeterministic, the envoirement will check who won
                // against the dealer and will pay them according to there bets
                isNextActionDeterministic = false;
                availableRandoms.clear();
                availableRandoms.add(BlackJackActionNonDet.PAYPLAYERS.getAction());

                break;
            case PAYPLAYERS: // this logic can prob be simplified later check first if the dealer is bust
                for (Player p : players) {
                    if (!p.equals(dealer)) {
                        for (Hand h : p.getHands()) { // iterating over each players hands
                            if (h.checkForBlackJack()) { // Player has a blackjack
                                if (dealer.getActiveHand().checkForBlackJack()) { // dealer has blackjack as well
                                    p.collect(p.getBetAmountForHand(h)); // push Player gets his bet back
                                } else { // only the player has a blackjack and gets payed 3 to 2
                                    p.collect(p.getBetAmountForHand(h) * 2.5);
                                }
                            } else if (!h.isBust()) { // notbust
                                if (p.getActiveHand().getHandValue() > dealer.getActiveHand().getHandValue()) {
                                    // if player wins against dealer
                                    p.collect(p.getBetAmountForHand(h) * 2);
                                } else if (p.getActiveHand().getHandValue() == dealer.getActiveHand().getHandValue()) { // push
                                    p.collect(p.getBetAmountForHand(h));
                                }
                            }
                        }
                    }
                }

                // Setup new Round
                for (Player p : players) {
                    p.clearHand();
                }
                playersTurn = 0;
                isNextActionDeterministic = true;
                break;
        }
    }

    /**
     * 
     * 
     * 
     * Logik: If(dealphase){ passToNext; deterministic = False; }
     * 
     * else if( playersTurn && handsize > 2){ switch(lastAction) case HIt,
     * DoubleDown, Split: check HandFinished = true oder false if (isHandFinished) {
     * // check for more hands if (currentPlayer.setNextHandActive() != null) { //
     * There are more hands isNextActionDeterministic = false; return; } else {
     * isNextActionDeterministic = true; passToNextPlayer(); } }else
     * isNextActionDeterministic = true }
     */

    public boolean checkForBlackJack(ArrayList<Card> hand) {
        return hand.get(0).rank.getValue() + hand.get(1).rank.getValue() == 21 && hand.size() == 2;
    }

    public boolean isNextPlayerDealer() {
        // refactor check for dealer
        return players[getNextPlayer()].equals(dealer);
    }

    public int getHandValue(ArrayList<Card> hand) {
        int result = 0;
        int aces = 0;
        for (Card c : hand) {
            if (c.rank.equals(Card.Rank.ACE)) {
                aces++;
            }
            result = c.rank.getValue();
        }
        for (int i = 0; i < aces; i++) {
            if (result > 21) {
                result -= 10;
            }
        }
        return result;
    }

    public boolean isBust(ArrayList<Card> hand) {
        return getHandValue(hand) > 21;
    }

    @Override
    public boolean isNextActionDeterministic() {
        return isNextActionDeterministic;

    }

    @Override
    public ACTIONS getNextNondeterministicAction() {
        if (isNextActionDeterministic) {
            return null;
        }
        return ACTIONS.fromInt(0);
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
        return new StateObserverBlackJack(this);
    }

    public Player getCurrentPlayer() {
        return players[getPlayer()];
    }

    public Player getDealer() {
        return dealer;
    }

    public Player[] getPlayers() {
        return players;
    }

}
