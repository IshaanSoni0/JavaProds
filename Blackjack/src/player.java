import java.util.ArrayList; // import the ArrayList class
public class player {
    private ArrayList<card> hand;
    private int money;
    private String name;

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
    public int bet() {
        //betting logic
        //bet a random percentage of the player's money
        // if the player has enough money, subtract the bet amount from their money
        //player has a chance to go all in, fold, or call or raise based on their hand
        //player makes desisions based on their hand and the dealer's hand
        return 0; // Placeholder for bet amount
    }
}