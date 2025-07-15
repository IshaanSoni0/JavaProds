import java.util.ArrayList; // import the ArrayList class
public class river {
    private ArrayList<card> riverCards;

    public river() {
        this.riverCards = new ArrayList<card>();
    }

    public void addCard(deck d) {
        if(riverCards.size() < 3) {
            for(int i = 0; i < 3; i++) {
                card c = d.getRandCard();
                riverCards.add(c);
            }
        }
        if (riverCards.size() == 3){
            card c = d.getRandCard();
            riverCards.add(c);
        }
    }
    public ArrayList<card> getRiverCards() {
        return riverCards;
    }
    public void clearRiver() {
        riverCards.clear();
    }
    public void showRiver() {
        System.out.println("River cards: ");
        for (card c : riverCards) {
            System.out.println(c.toString());
        }
    }
}
