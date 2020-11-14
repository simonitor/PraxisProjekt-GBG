package games.BlackJack;

import java.util.ArrayList;

public class Player {
    ArrayList<ArrayList<Card>> hands; // only needed if hands are split
    ArrayList<Card> hand = new ArrayList<Card>();
    private int chips = 1500;
    private int betThisRound = 0;
    private boolean splitHand = false;
    String name;

    public Player(String name) {
        this.name = name;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void addCardToHand(Card c) {
        hand.add(c);
    }

    public void addCardToSpecificHand(Card c, int handIndex){
        hands.get(handIndex).add(c);
    }

    public ArrayList<Card> getSpecificHand(int index){
        if(hasSplitHand()){
            return hands.get(index);
        }
        return hand;
    }
    public void bet(int amount) {
        chips -= amount;
        betThisRound += amount;
    }

    public void clearHand() {
        hand.clear();
        hands.clear();
        splitHand = false;
    }

    public String toString() {
        return name;
    }

    public int betThisRound() {
        return betThisRound;
    }

    public boolean hasHand() {
        return !hand.isEmpty();
    }

    public int getChips() {
        return chips;
    }

    public boolean hasSplitHand(){
        return splitHand;
    }

    public void splitHand(){
        splitHand = true;
        //todo logic implementieren
    }

}