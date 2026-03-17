import java.util.ArrayList; // import the ArrayList class

public class deck {
    ArrayList<card> deck = new ArrayList<card>();    
    public deck(){
        for(int s = 0; s<4; s++){
            for(int v = 1; v<14;v++){
                card card = new card(s,v);
                deck.add(card);
            }
        }
    }

    public int getlength(){
        return deck.size();
    }
    public card getRandCard(){
        if (deck.size() == 0) {
            // Reinitialize deck if empty to avoid IndexOutOfBoundsException
            deck = new ArrayList<card>();
            for(int s = 0; s<4; s++){
                for(int v = 1; v<14;v++){
                    card card = new card(s,v);
                    deck.add(card);
                }
            }
        }
        int rand = (int)(Math.random() * deck.size());
        card c = deck.get(rand);
        deck.remove(rand);
        return c;
    }
}
