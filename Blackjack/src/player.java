import java.util.ArrayList; // import the ArrayList class
public class player {
    private ArrayList<card> hand;
    private int money;
    private String name;
    private int bet;
    private int handValue;

    public player(String name) {
        this.hand = new ArrayList<card>();
        this.money = 100; // Default starting money
        this.name = name;
    }

    public void addCard(deck d) {
        card c = d.getRandCard();
        hand.add(c);
    }

    public ArrayList<card> getHand() {
        return hand;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int money) {
        this.money += money;
    }

    public void clearHand() {
        hand.clear();
    }
    public void setMoney(int money) {
        this.money = money;
    }
    public void showHand() {
        System.out.println(name+"'s hand is: ");
        for (card c : hand) {
            System.out.println(c.toString());
        }
    }
    public void showMoney() {
        System.out.println(name+" has $" + money + ".");
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

 
    public int getCombinedHandValue(ArrayList<card> riverCards) {
    //sees what the highest hand the player has based on the cards in their hand and the dealers hand
    // possible hands: high card, pair, two pair, three of a kind, straight, flush, full house, four of a kind, straight flush, royal flush
    //hand value is the hand the player has based on the cards in their hand and the river
    //----------------------------------------------------------------------------------------------------------
    ArrayList<card> allCards = new ArrayList<card>();
    allCards.addAll(hand);
    allCards.addAll(riverCards);

    // Count occurrences of each value and suit
    int[] valueCount = new int[15]; // 1-13, 14 for Ace high
    int[] suitCount = new int[4];   // 0:hearts, 1:diamonds, 2:spades, 3:clubs
    ArrayList<Integer> cardValues = new ArrayList<Integer>();

    for (card c : allCards) {
        int val = c.Cvalue;
        valueCount[val]++;
        if (val == 1) valueCount[14]++; // Ace as high

        if (c.Csuit.equals("hearts")) suitCount[0]++;
        if (c.Csuit.equals("dimonds")) suitCount[1]++;
        if (c.Csuit.equals("spades")) suitCount[2]++;
        if (c.Csuit.equals("clubs")) suitCount[3]++;

        if (!cardValues.contains(val)) cardValues.add(val);
        if (val == 1 && !cardValues.contains(14)) cardValues.add(14); // Ace high
    }

    cardValues.sort(null);

    // Check for flush
    boolean isFlush = false;
    String flushSuit = "";
    for (int i = 0; i < 4; i++) {
        if (suitCount[i] >= 5) {
            isFlush = true;
            if (i == 0) flushSuit = "hearts";
            if (i == 1) flushSuit = "dimonds";
            if (i == 2) flushSuit = "spades";
            if (i == 3) flushSuit = "clubs";
            break;
        }
    }

    // Check for straight
    boolean isStraight = false;
    int straightHigh = 0;
    int consecutive = 0;
    for (int i = 1; i <= 14; i++) {
        if (valueCount[i] > 0) {
            consecutive++;
            if (consecutive >= 5) {
                isStraight = true;
                straightHigh = i;
            }
        } else {
            consecutive = 0;
        }
    }

    // Check for straight flush / royal flush
    if (isFlush) {
        ArrayList<Integer> flushVals = new ArrayList<Integer>();
        for (card c : allCards) {
            if (c.Csuit.equals(flushSuit)) {
                flushVals.add(c.Cvalue);
                if (c.Cvalue == 1 && !flushVals.contains(14)) flushVals.add(14); // Ace high
            }
        }
        flushVals.sort(null);

        int flushConsec = 0;
        int prev = -1;
        for (int val : flushVals) {
            if (prev == -1 || val == prev + 1) {
                flushConsec++;
                if (flushConsec >= 5) {
                    if (val == 14) return 10; // Royal flush
                    return 9; // Straight flush
                }
            } else if (val != prev) {
                flushConsec = 1;
            }
            prev = val;
        }
    }

    // Four of a kind
    for (int i = 1; i <= 13; i++) {
        if (valueCount[i] == 4) return 8;
    }

    // Full house (must be different values for three and two)
    int threeVal = -1, twoVal = -1;
    for (int i = 1; i <= 13; i++) {
        if (valueCount[i] >= 3) threeVal = i;
    }
    for (int i = 1; i <= 13; i++) {
        if (valueCount[i] >= 2 && i != threeVal) twoVal = i;
    }
    if (threeVal != -1 && twoVal != -1) return 7;

    // Flush
    if (isFlush) return 6;

    // Straight
    if (isStraight) return 5;

    // Three of a kind (not full house)
    for (int i = 1; i <= 13; i++) {
        if (valueCount[i] == 3 && i != threeVal) return 4;
    }
    if (threeVal != -1) return 4;

    // Two pair
    int pairCount = 0;
    for (int i = 1; i <= 13; i++) {
        if (valueCount[i] == 2) pairCount++;
    }
    if (pairCount >= 2) return 3;

    // One pair
    if (pairCount == 1) return 2;

    // High card
    return 1;
}
}