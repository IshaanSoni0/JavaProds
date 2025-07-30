import java.util.ArrayList;

public class playBlackjack {
    deck deck = new deck();
    ArrayList<card> playerHand;
    ArrayList<card> dealerHand;
    int playerMoney; // Initialize player's money
    boolean playerBusted = false;
    boolean dealerBusted = false;
    int playerBet;
    int trueCount = 0; // Initialize true count for card counting

    public void resetTrueCount() {
        this.trueCount = 0; // Reset true count
    }
    public int getTrueCount() {
        return trueCount; // Getter for true count
    }
    public void updateTrueCount(card c) {
        int value = c.hiLowValue(c);
        if (value == 1) {
            trueCount++; // Increase true count for low cards
        } else if (value == -1) {
            trueCount--; // Decrease true count for high cards
        }
    }
    public void setBet(int bet){
        this.playerBet = bet;
    }
    public int getBet() {
        return playerBet;
    }
    public void loseWin(boolean win) {
        if (win) {
            playerMoney += playerBet;
        } else {
            playerMoney -= playerBet;
        }

    }
    public void setMoney(int money) {
        this.playerMoney = money;
    }
    public int getMoney() {
        return playerMoney;
    }
    public void resetgame(){
        playerHand = new ArrayList<card>();
        dealerHand = new ArrayList<card>();
    }
    public void resetDeck() {
        deck = new deck(); // Reinitialize the deck
    }
    public void dealDealer(){
        dealerHand.add(deck.getRandCard());
    }
    public void dealPlayer(){
        playerHand.add(deck.getRandCard());
    }
    public ArrayList<card> getPlayerHand() {
        return playerHand;
    }
    public card getPlayerCard(int index) {
        return playerHand.get(index);
    }
    public ArrayList<card> getDealerHand() {
        return dealerHand;
    }
    public card getDealerCard(int index) {
        return dealerHand.get(index);
    }
    public void hitPlayer() {
        playerHand.add(deck.getRandCard());
    }
    public void hitDealer() {
        dealerHand.add(deck.getRandCard());
    }
    public int getPlayerTotal() {
        int total = 0;
        int aceCount = 0;
        for (card c : playerHand) {
            int value = c.getValue();
            total += value;
            if (value == 1) aceCount++;
        }
        // Upgrade Ace(s) from 1 to 11 if it doesn't bust
        while (aceCount > 0 && total + 10 <= 21) {
            total += 10;
            aceCount--;
        }
        return total;
    }

    public int getDealerTotal() {
        int total = 0;
        int aceCount = 0;
        for (card c : dealerHand) {
            int value = c.getValue();
            total += value;
            if (value == 1) aceCount++;
        }
        // Upgrade Ace(s) from 1 to 11 if it doesn't bust
        while (aceCount > 0 && total + 10 <= 21) {
            total += 10;
            aceCount--;
        }
        return total;
    }
    public boolean playerBusted() {
        playerBusted = getPlayerTotal() > 21;
        return getPlayerTotal() > 21;
    }
    public boolean dealerBusted() {
        dealerBusted = getDealerTotal() > 21;
        return getDealerTotal() > 21;
    }
    public void setPlayerHand(java.util.List<card> hand) {
        this.playerHand = new ArrayList<>(hand);
    }
}
