package games.BlackJack;

public class Card {

    enum Suit {
        HEART, DIAMOND, CLUB, SPADE;
    }

    enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10),
        ACE(11);

        private int value;

        Rank(int valuel) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    Rank rank;
    Suit suit;
    int uniqueId;

    public Card(Rank rank, Suit suit, int uniqueId) {
        this.rank = rank;
        this.suit = suit;
        this.uniqueId = uniqueId;
    }
}