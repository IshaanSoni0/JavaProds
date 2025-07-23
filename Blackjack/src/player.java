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
    public int getCombinedHandValue(){
        //sees what the highest hand the player has based on the cards in their hand and the dealers hand
        // possible hands: high card, pair, two pair, three of a kind, straight, flush, full house, four of a kind, straight flush, royal flush
        //hand value is the hand the player has based on the cards in their hand and the river
        //----------------------------------------------------------------------------------------------------------
        



        return 0; // Placeholder for actual hand value calculation
        
    }

}