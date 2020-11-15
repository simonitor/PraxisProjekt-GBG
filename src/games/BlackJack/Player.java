package games.BlackJack;

import java.util.ArrayList;

public class Player {
    ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
    ArrayList<Card> activeHand = new ArrayList<Card>();
    private int chips = 1500;
    private int betThisRound[] = new int[24];
    private boolean splitHand = false;
    private int activeHandIndex = 0;

    String name;

    public Player(String name) {
        this.name = name;
    }

    public ArrayList<Card> getActiveHand() {
        return activeHand;
    }

    public ArrayList<ArrayList<Card>> getHands() {
        return hands;
    }

    public void addCardToActiveHand(Card c) {
        if (activeHand.isEmpty()) {
            hands.add(activeHand);
        }
        activeHand.add(c);
    }

    public ArrayList<Card> setNextHandActive() {
        if (hands.size() < activeHandIndex + 1) {
            return null;
        }
        return activeHand = hands.get(activeHandIndex);
    }

    public void bet(int amount) {
        chips -= amount;
        betThisRound[activeHandIndex] += amount;
    }

    public void clearHand() {
        activeHand.clear();
        hands.clear();
        betThisRound = new int[24];
        activeHandIndex = 0;
        splitHand = false;
    }

    public String toString() {
        return name;
    }

    public int betOnActiveHand() {
        return betThisRound[activeHandIndex];
    }

    public boolean hasHand() {
        return !hands.isEmpty();
    }

    public int getChips() {
        return chips;
    }

    public boolean hasSplitHand() {
        return splitHand;
    }

    public void splitHand() {
        splitHand = true;
        ArrayList<Card> nHand = new ArrayList<Card>();
        nHand.add(activeHand.remove(1));
        hands.add(nHand);
    }

}