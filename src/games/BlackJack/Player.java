package games.BlackJack;

import java.util.ArrayList;

public class Player {
    ArrayList<Hand> hands = new ArrayList<Hand>();
    Hand activeHand = null;
    private double chips = 1500;
    private double betThisRound[] = new double[24];
    private boolean splitHand = false;
    private int activeHandIndex = 0;

    String name;

    public Player(String name) {
        this.name = name;
    }

    public Hand getActiveHand() {
        return activeHand;
    }

    public ArrayList<Hand> getHands() {
        return hands;
    }

    public void addCardToActiveHand(Card c) {
        if (activeHand == null) {
            activeHand = new Hand(c);
            hands.add(activeHand);
        } else {
            activeHand.addCard(c);
        }
    }

    public Hand setNextHandActive() {
        if (hands.size() < activeHandIndex + 1) {
            return null;
        }
        return activeHand = hands.get(++activeHandIndex);
    }

    public void bet(double amount) {
        chips -= amount;
        betThisRound[activeHandIndex] += amount;
    }

    public void clearHand() {
        activeHand = null;
        hands.clear();
        betThisRound = new double[24];
        activeHandIndex = 0;
        splitHand = false;
    }

    public String toString() {
        return name;
    }

    public double betOnActiveHand() {
        return betThisRound[activeHandIndex];
    }

    public double getBetAmountForHand(Hand h) {
        int index = hands.indexOf(h);
        return betThisRound[index];
    }

    public boolean hasHand() {
        return !hands.isEmpty();
    }

    public double getChips() {
        return chips;
    }

    public boolean hasSplitHand() {
        return splitHand;
    }

    public double collect(double chips) {
        return this.chips += chips;
    }

    public void splitHand() {
        splitHand = true;
        hands.add(activeHand.split());
        betThisRound[hands.size() - 1] = betThisRound[activeHandIndex];
        chips -= betThisRound[activeHandIndex];
    }

}