package games.BlackJack;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import games.ObserverBase;
import games.StateObsNondeterministic;
import games.StateObservation;
import tools.ScoreTuple;
import tools.Types.ACTIONS;
import tools.Types;

public class StateObserverBlackJack extends ObserverBase implements StateObsNondeterministic {

    // First version only features 1 Player vs the Dealer, N Players vs Dealer will
    // be added later.

    StateObserverBlackJack test = this;
    private static final long serialVersionUID = 1L;
    private final int NUM_PLAYERS = 2;
    private ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>();
    private Player currentPlayer;
    private boolean isNextActionDeterministic = true;
    private ArrayList<Integer> availableRandoms = new ArrayList<Integer>();
    private Deck deck = new Deck();
    private Dealer dealer;
    private Player players[] = new Player[NUM_PLAYERS];
    private int playersTurn;
    private gamePhase gPhase = gamePhase.BETPHASE;
    private boolean playerActedInPhase[] = new boolean[NUM_PLAYERS];
    private ArrayList<String> log = new ArrayList<String>();
    private transient GameBoardBlackJackGui bjGui;
    private int currentSleepDuration = 0;

    public StateObserverBlackJack(GameBoardBlackJackGui bjGui) {
        // defaultState
        // adding dealer and player/s
        this.bjGui = bjGui;
        dealer = new Dealer("dealer");
        for (int i = 0; i < players.length; i++) {
            this.players[i] = new Player("p" + i);
        }
        playersTurn = 0;
        currentPlayer = getCurrentPlayer();
        setAvailableActions();
    }


    public StateObserverBlackJack(StateObserverBlackJack other) {
        super(other);
        this.playersTurn = other.playersTurn;
        this.dealer = new Dealer(other.dealer);
        this.availableRandoms = new ArrayList<>(other.availableRandoms);
        this.availableActions = new ArrayList<>(other.availableActions);
        this.isNextActionDeterministic = other.isNextActionDeterministic;
        this.currentPlayer = getCurrentPlayer();
        for (int i = 0; i < players.length; i++) {
            this.players[i] = new Player(other.players[i]);
        }
        this.gPhase = other.gPhase;
        this.playerActedInPhase = other.playerActedInPhase.clone();
        this.log = new ArrayList<>(other.log);
        this.bjGui = other.bjGui;
        this.currentSleepDuration = other.currentSleepDuration;
    }

    // mapping Deterministic actions to ENUMS to get more readable code
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

    // mapping NonDeterministic actions to ENUMS
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

    // Enum Gamephases to keep track of the phase the game is in
    // BPHASE -> Players need to place a bet before they get cards
    // DEALPHASE -> Players and Dealer get dealt 2 cards
    // PLAYERONACTION -> Players play their hand(s)
    // DEALERONACTION -> Dealer Plays his hand (nondetermenistic)
    // PAYOUT -> Determin which players won against the dealer and paying them
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

    enum results {
        WIN, PUSH, LOSS, SURRENDER, BLACKJACK;
    }

    enum PartialStateMode {
        THIS_PLAYER, WHATS_ON_TABLE, FULL;
    }

    public StateObservation partialState(PartialStateMode mode) {
        switch (mode) {
            case THIS_PLAYER:
            case WHATS_ON_TABLE:
                if (gPhase != gamePhase.DEALERONACTION && gPhase != gamePhase.PAYOUT) {
                    StateObserverBlackJack p_so = new StateObserverBlackJack(this);
                    if (dealer.hasHand() && dealer.getActiveHand().size() == 2) {
                        p_so.dealer.activeHand.getCards().remove(1);
                        p_so.dealer.activeHand.getCards().add(new Card(Card.Rank.X, Card.Suit.X, 999));
                    }
                    return p_so;
                } else {
                    return this;
                }
            case FULL:
                return this;
        }
        return this;
    }

    @Override
    public StateObservation clearedCopy() {
        return null;
    }

    @Override
    public boolean isGameOver() {
        for (Player p : players){
            if(p.getChips() > 0) {
                return false;
            }
        }
        return true;
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
        for (Player p : players){
            if(p.getChips() < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean stopInspectOnGameOver() {
        return false;
    }

    @Override
    public String stringDescr() {
        String result = "";
        for (Player p : players) {
            result += p;
        }
        result += "player to move: " + players[getPlayer()].name + "\nhis available actions :\n";
        for (Types.ACTIONS a : getAllAvailableActions()) {
            result += BlackJackActionDet.values()[a.toInt()].name() + " - ";
        }
        result = "dealer : " + dealer.getActiveHand();
        return result;
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
        // splittinig the advance into
        // Deterministic
        if (isNextActionDeterministic()) {
            advanceDeterministic(action);
        }
        // and NonDeterministic
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
            // insurance
            if (dealer.getActiveHand().getCards().get(0).rank.equals(Card.Rank.ACE)
                    && currentPlayer.insuranceAmount() == 0) {
                availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.INSURANCE.getAction()));
            }
            if (!currentPlayer.getActiveHand().isHandFinished()) {
                // enters after Player has placed his bet
                // player is not bust nor got 21 nor got a blackjack because the hand is not
                // fnished

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
                // Surrender - player surrenders his hand and gets half of his chips back
                // Condition: needs to be first action of hand
                if(playersHand.size() == 2 && !currentPlayer.hasSplitHand()) {
                    availableActions.add(Types.ACTIONS.fromInt(BlackJackActionDet.SURRENDER.getAction()));
                }
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
        return ACTIONS.fromInt(i);
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
        // convert action to enum
        BlackJackActionDet a = BlackJackActionDet.values()[action.toInt()];
        // store as last move
        addToLastMoves(action);
        log.add(currentPlayer.name + " chose action : " + a);
        isNextActionDeterministic = true;

        // this switch only changes player attributes caused by the action
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
            case SURRENDER:
                currentPlayer.surrender();
                currentPlayer.collect(currentPlayer.getBetAmountForHand(currentPlayer.getActiveHand()) / 2);
                break;
            case INSURANCE:
                currentPlayer.insurance();
                break;
            default:
                break;
        }
        // this switch determins if the next action is determinisic or nondeterministic,
        // if it is the next players turn and if the gamePhase advences
        switch (a) {
            case BET1:
            case BET5:
            case BET10:
            case BET25:
            case BET50:
            case BET100:
            case STAND:
            case SURRENDER:
                if (currentPlayer.setNextHandActive() != null) { // has player more hands? only important in case stand
                    isNextActionDeterministic = false; // if yes the next hand is active now, Split hands always have
                                                       // only one card so the next hand needs to get dealt one more
                                                       // card. Next action is nondeterministic because the card dealt
                                                       // is random
                } else { // player has no more hands
                    passToNextPlayer(); // pass to next
                    isNextActionDeterministic = !everyPlayerActed(); // if every player acted == true, its dealers turn.
                                                                     // Dealers turn is nondeterministic. Otherwise its
                                                                     // next players turn, next action is deterministic
                    if (everyPlayerActed()) { // advance the gamePhase
                        advancePhase();
                    }
                }
                break;
            case HIT:
            case DOUBLEDOWN:
            case SPLIT:
                isNextActionDeterministic = false;
                break;
            case INSURANCE:
                isNextActionDeterministic = true;
            default:
                break;
        }
        if (isNextActionDeterministic) { // we only need to set actions if the next action is deterministic
            setAvailableActions();
        }

    }

    public void setAvailableRandoms() { // the gamephase determins which nondeterministic action is triggered
                                        // problem because it is not randomly chosen??????
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

    public boolean isDealPhase() { // deprecated?
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

        switch (a) {
            case DEALCARD: // a card gets dealt
                currentPlayer = getCurrentPlayer();
                switch (gPhase) {

                    case DEALPHASE:
                        // in dealphase the cards get dealt 1 by 1
                        if (everyPlayerActed()) { // if true dealer gets a card
                            dealer.addCardToActiveHand(deck.draw());
                            playerActedInPhase = new boolean[NUM_PLAYERS];
                            if (dealer.getActiveHand().size() == 2) { // if dealer has 2 cards dealing is over advance
                                                                      // into next phase (next action det)
                                advancePhase();
                                isNextActionDeterministic = true;
                            }
                        } else { // every player gets one card
                            currentPlayer.addCardToActiveHand(deck.draw());
                            passToNextPlayer();
                            isNextActionDeterministic = false;
                        }
                        break;

                    case PLAYERONACTION:
                        // nonDeterministic advance that a player triggered by his last action
                        Hand currentHand = currentPlayer.getActiveHand();
                        int f = getLastMove();
                        // get the last deterministic action
                        Types.ACTIONS i = Types.ACTIONS.fromInt(f);
                        BlackJackActionDet lastAction = BlackJackActionDet.values()[i.toInt()];
                        // every NonDeterministicAdvance triggered by a players action is about getting
                        // a card dealt
                        Card newCard = deck.draw();
                        log.add(currentPlayer.name + " gets a card dealt: " + newCard);
                        currentHand.addCard(newCard);

                        // consequence of the card being dealt and the last action
                        switch (lastAction) {
                            case DOUBLEDOWN:
                                currentHand.setHandFinished();
                                addToLastMoves(Types.ACTIONS.fromInt(BlackJackActionDet.STAND.action));
                                break;
                        }

                        if (currentHand.isHandFinished()) { // checking if the hand is finished, manualy finished or
                                                            // handvalue > 20
                            if (currentPlayer.setNextHandActive() != null) { // check for more hands
                                isNextActionDeterministic = false; // if more hands, the next hand needs to get dealt
                                                                   // one more card for sure (nondet)
                            } else { // no more hands, pass to next
                                passToNextPlayer();
                                isNextActionDeterministic = true;
                                if (everyPlayerActed()) { // if everyone acted the advance to next gamePhase
                                    advancePhase();
                                    isNextActionDeterministic = false; // dealer will play his hand, envoirement nondet
                                }
                            }
                        } else { // players hand is not finished, still same players turn next action det
                            isNextActionDeterministic = true;
                        }
                        break;

                    default:
                        System.out.print("err ");
                        break;

                }
                break;

            case DEALERPLAYS:
                log.add("Dealer reaveals card " + dealer.getActiveHand().getCards().get(1));
                while (dealer.getActiveHand().getHandValue() < 17) {
                    // The dealer will always hit under 17 and will always stay on 17 or higher,
                    // even if the opponent got 18, dealer got 17 and there is only this one
                    // opponent
                    Card newCard = deck.draw();
                    log.add("Dealer gets a card dealt: " + newCard);
                    dealer.activeHand.addCard(newCard);

                }
                advancePhase(); // dealer finished advance to next gamePhase
                isNextActionDeterministic = false;
                break;

            case PAYPLAYERS:
                for (Player p : players) {

                    if (p.hasSurrender()) { // if the player surrendered the payout already happened
                        log.add(p.name + " " + results.SURRENDER + " vs dealer: " + dealer.getActiveHand()
                                + " handvalue: " + dealer.getActiveHand().getHandValue());
                    } else {
                        for (Hand h : p.getHands()) {
                            // case blackjack
                            results r = results.LOSS;
                            double amountToCollect = 0;
                            double bet = p.getBetAmountForHand(h);
                            if (dealer.getActiveHand().checkForBlackJack()) {
                                if(p.insuranceAmount() > 0) {
                                    log.add(p.name + " gets paid insurance collected: " + p.insuranceAmount() * 2);
                                    p.collect(p.insuranceAmount() * 2);
                                }
                            }
                            if (h.checkForBlackJack() && !p.hasSplitHand()) { // player has blackjack, if player got a
                                                                              // blackjack in a split hand it does not
                                                                              // count
                                                                              // as blackjack
                                amountToCollect = bet; // both have blackjack push/draw player gets bet back
                                r = results.PUSH;
                                if (!dealer.getActiveHand().checkForBlackJack()) { // dealer has no blackjack
                                    amountToCollect = bet * 2.5;
                                    r = results.BLACKJACK;
                                }
                            } else { // player has no blackjack
                                if (!h.isBust()) { // player not bust
                                    if (dealer.getActiveHand().isBust()) {// dealer is bust player not
                                        amountToCollect = bet * 2;
                                        r = results.WIN;
                                    } else if(!dealer.getActiveHand().checkForBlackJack()){
                                        if (h.getHandValue() > dealer.getActiveHand().getHandValue()) { // player wins
                                            amountToCollect = bet * 2;
                                            r = results.WIN;
                                        } else if (h.getHandValue() == dealer.getActiveHand().getHandValue()) { // push/draw
                                                                                                                // player
                                                                                                                // gets
                                                                                                                // his
                                                                                                                // bet
                                                                                                                // back
                                            amountToCollect = bet;
                                            r = results.PUSH;
                                        }
                                    }
                                }
                            }

                            p.collect(amountToCollect);
                            log.add(p.name + " hand: " + h + " val=" + h.getHandValue() + " " + r + " vs. dealer: "
                                    + dealer.getActiveHand() + " val=" + dealer.getActiveHand().getHandValue()
                                    + " collected: " + amountToCollect + " chips");

                        }
                    }
                }
                log.add("-------------New Round---------------");

                if(currentSleepDuration > 0) {
                    bjGui.update(this, false, false);
                    SwingUtilities.invokeLater(() -> {
                        bjGui.updateWithSleep((StateObserverBlackJack) test.partialState(PartialStateMode.THIS_PLAYER), currentSleepDuration);
                        bjGui.revalidate();
                        bjGui.repaint();
                    });
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

    public void updateCurrentSleepDuration(int milliSeconds){
        currentSleepDuration = milliSeconds;
    }

    public Player getCurrentPlayer() {
        return players[getPlayer()];
    }

    public Dealer getDealer() {
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

    public StateObserverBlackJack.gamePhase getCurrentPhase() {
        return gPhase;
    }

    public boolean everyPlayerActed() {
        for (boolean a : playerActedInPhase) {
            if (!a) {
                return false;
            }
        }
        return true;
    }

    public boolean dealersTurn() {
        return (gPhase == gamePhase.DEALERONACTION || gPhase == gamePhase.PAYOUT);
    }


    public ArrayList<String> getHandHistory() {
        return log;
    }

}
