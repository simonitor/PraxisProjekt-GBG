package games.BlackJack;

import java.util.ArrayList;
import java.util.Random;

public class Deck {

    ArrayList<Card> deck;

    public Deck() {
        deck = new ArrayList<Card>();
        int count = 0;
        for (int i = 0; i < 6; i++) { // 6 card decks (common in Blackjack)
            for (Card.Suit s : Card.Suit.values()) { // create every permutation
                for (Card.Rank r : Card.Rank.values()) {
                    deck.add(new Card(r, s, count++));
                }
            }
        }
    }

    public Deck(Deck other) {
        this.deck = new ArrayList<>(other.deck);
    }

    public Deck(ArrayList<Card> deck) {
        this.deck = deck;
    }

    public int size() {
        return deck.size();
    }

    public Card draw() {
        Random r = new Random();
        // returns random card between zero and decksize
        return deck.remove(r.ints(0, (size() + 1 - 1)).findFirst().getAsInt());
    }

}