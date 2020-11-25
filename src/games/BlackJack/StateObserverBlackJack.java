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
    private int NUM_PLAYERS = 1;
    private Player p1;
    private ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>();
    private Player currentPlayer;
    private boolean isNextActionDeterministic = true;
    private ArrayList<Integer> availableRandoms = new ArrayList<Integer>();
    private Deck deck = new Deck();
    private Player dealer;
    private Player players[] = new Player[1];
    private int playersTurn;
    private gamePhase gPhase = gamePhase.BETPHASE;
    private boolean playerActedInPhase[] = new boolean[NUM_PLAYERS];
    private ArrayList<String> handHistory = new ArrayList<String>();

    public StateObserverBlackJack() {
        // defaultState
        // adding dealer and player/s
        dealer = new Player("dealer");
        p1 = new Player("p1");
        players[0] = p1;
        playersTurn = 0;
        currentPlayer = getCurrentPlayer();
        setAvailableActions();
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
        this.gPhase = other.gPhase;
        this.playerActedInPhase = other.playerActedInPhase.clone();
        this.handHistory = new ArrayList<>(other.handHistory);
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

    enum gamePhase {
        BETPHASE(0), DEALPHASE(1), PLAYERONACTION(2), DEALERONACTION(3), PAYOUT(4);

        private int phase;

        private gamePhase(int phase) {
            this.phase = phase;
        }

        public int getPhase() {
            return this.phase;
        }

        public gamePhase getNext() {
            return gamePhase.values()[(phase + 1) % gamePhase.values().length];
        }
    }

    @Override
    public StateObservation clearedCopy() {
        return null;
    }

    @Override
    public boolean isGameOver() {
        return false;
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
        return "xyz";
    }

    @Override
    public String stringActionDescr(ACTIONS act) {
        return BlackJackActionDet.values()[act.toInt()].name();
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
        return 50;
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
        currentPlayer = getCurrentPlayer();
        if (gPhase.equals(gamePhase.BETPHASE)) {
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
        } else if (gPhase.equals(gamePhase.PLAYERONACTION)) {
            if (!currentPlayer.getActiveHand().isHandFinished()) {
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

    @Override
    public void setPlayer(int p) {
        playersTurn = p;
    }

    @Override
    public int getCreatingPlayer() {
        return 0;
    }

    @Override
    public int getNumPlayers() {
        return NUM_PLAYERS;
    }

    @Override
    public void advanceDeterministic(ACTIONS action) {
        currentPlayer = getCurrentPlayer();
        BlackJackActionDet a = BlackJackActionDet.values()[action.toInt()];
        addToLastMoves(action);
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
            case DOUBLEDOWN:
                currentPlayer.bet(currentPlayer.betOnActiveHand());
                break;
            case SPLIT:
                currentPlayer.splitHand();
                break;
            default:
                break;
        }

        switch (a) {
            case BET1:
            case BET5:
            case BET10:
            case BET25:
            case BET50:
            case BET100:
            case STAND:
                if (currentPlayer.setNextHandActive() != null) {
                    isNextActionDeterministic = false;
                } else {
                    passToNextPlayer();
                    isNextActionDeterministic = !everyPlayerActed(); // immer false für 1 spieler
                    if (everyPlayerActed()) {
                        advancePhase();
                    }
                }
                break;
            case HIT:
            case DOUBLEDOWN:
            case SPLIT:
                isNextActionDeterministic = false;
                break;
            default:
                break;
        }
        if (isNextActionDeterministic) {
            setAvailableActions();
        }

    }

    public void setAvailableRandoms() {
        availableRandoms.clear();
        switch (gPhase) {
            case DEALPHASE:
            case PLAYERONACTION:
                availableRandoms.add(BlackJackActionNonDet.DEALCARD.getAction());
                break;
            case DEALERONACTION:
                availableRandoms.add(BlackJackActionNonDet.DEALERPLAYS.getAction());
                break;
            case PAYOUT:
                availableRandoms.add(BlackJackActionNonDet.PAYPLAYERS.getAction());
                break;
            default:
                break;
        }

    }

    public void advancePhase() {
        gPhase = gPhase.getNext();
        playerActedInPhase = new boolean[NUM_PLAYERS];
        setPlayer(0);
    }

    public boolean isDealPhase() {
        // dealers handsize < 2 ?
        if (!dealer.hasHand()) {
            return true;
        }
        return dealer.getActiveHand().size() < 2;
    }

    @Override
    public void advanceNondeterministic() {
        advanceNondeterministic(getNextNondeterministicAction());
    }

    @Override
    public void advanceNondeterministic(ACTIONS action) {
        if (isNextActionDeterministic) {
            throw new RuntimeException("Next action should be deterministic");
        }
        BlackJackActionNonDet a = BlackJackActionNonDet.values()[action.toInt()];

        // feststellen ob next act det
        // feststellen ob an naechsten spieler weiter
        // tracken ob jeder spieler dran war in der phase -> phase anpassen

        switch (a) {
            case DEALCARD:
                currentPlayer = getCurrentPlayer();
                switch (gPhase) {

                    case DEALPHASE:
                        if (everyPlayerActed()) {
                            dealer.addCardToActiveHand(deck.draw());
                            playerActedInPhase = new boolean[NUM_PLAYERS];
                            if (dealer.getActiveHand().size() == 2) {
                                advancePhase();
                                isNextActionDeterministic = true;
                            }
                        } else {
                            currentPlayer.addCardToActiveHand(deck.draw());
                            passToNextPlayer();
                            isNextActionDeterministic = false;
                        }
                        break;

                    case PLAYERONACTION:
                        Hand currentHand = currentPlayer.getActiveHand();
                        int f = getLastMove();
                        Types.ACTIONS i = Types.ACTIONS.fromInt(f);
                        BlackJackActionDet lastAction = BlackJackActionDet.values()[i.toInt()];
                        currentHand.addCard(deck.draw());
                        switch (lastAction) {
                            case DOUBLEDOWN:
                                currentHand.setHandFinished();
                                break;
                        }

                        if (currentHand.isHandFinished()) {
                            if (currentPlayer.setNextHandActive() != null) {
                                isNextActionDeterministic = false;
                            } else {
                                passToNextPlayer();
                                isNextActionDeterministic = true;
                                if (everyPlayerActed()) {
                                    advancePhase();
                                    isNextActionDeterministic = false;
                                }
                            }
                        } else {
                            isNextActionDeterministic = true;
                        }
                        break;

                    default:
                        System.out.print("ärror xD");
                        break;

                }

                break;
            case DEALERPLAYS:
                while (dealer.getActiveHand().getHandValue() < 17) {
                    // The dealer will always hit under 17 and will always stay on 17 or higher,
                    // even if the opponent got 18, dealer got 17 and there is only this one
                    // opponent
                    dealer.activeHand.addCard(deck.draw());
                }
                advancePhase();
                isNextActionDeterministic = false;
                break;
            case PAYPLAYERS:
                for (Player p : players) {
                    for (Hand h : p.getHands()) {
                        // case blackjack
                        double amountToCollect = 0;
                        double bet = p.getBetAmountForHand(h);
                        if (h.checkForBlackJack()) { // player has blackjack
                            amountToCollect = bet; // both have blackjack push/draw player gets bet back
                            if (!dealer.getActiveHand().checkForBlackJack()) { // dealer has no blackjack
                                amountToCollect = bet * 2.5;
                            }
                        } else { // player has no blackjack
                            if (!h.isBust()) { // player not bust
                                if (dealer.getActiveHand().isBust()) {// dealer is bust player not
                                    amountToCollect = bet * 2;
                                } else {// both not bust
                                    if (h.getHandValue() > dealer.getActiveHand().getHandValue()) { // player wins
                                        amountToCollect = bet * 2;
                                    } else if (h.getHandValue() == dealer.getActiveHand().getHandValue()) { // push/draw
                                                                                                            // player
                                                                                                            // gets his
                                                                                                            // bet back
                                        amountToCollect = bet;
                                    }
                                }
                            }
                        }
                        p.collect(amountToCollect);
                        handHistory.add(p + " hand: " + h + " handvalue: " + h.getHandValue() + " dealer: "
                                + dealer.getActiveHand() + " handvalue: " + dealer.getActiveHand().getHandValue()
                                + " collected: " + amountToCollect + " chips");
                    }
                }

                // Setup new Round
                for (Player p : players) {
                    p.clearHand();
                }
                dealer.clearHand();
                advancePhase();
                isNextActionDeterministic = true;
                setAvailableActions();
                break;
        }
        if (isNextActionDeterministic)
            setAvailableActions();
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
        setAvailableRandoms();
        return Types.ACTIONS.fromInt(availableRandoms.remove(0));
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

    @Override
    public void passToNextPlayer() {
        playerActedInPhase[getPlayer()] = true;
        setPlayer(getNextPlayer());
    }

    public boolean everyPlayerActed() {
        for (boolean a : playerActedInPhase) {
            if (!a) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<String> getHandHistory() {
        return handHistory;
    }

}
