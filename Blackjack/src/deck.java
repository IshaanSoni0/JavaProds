import java.util.ArrayList; // import the ArrayList class

public class deck {
    ArrayList<String> deck = new ArrayList<String>();    
    public void make(){
        for(int i = 1; i<=4; i++){
            for(int j = 2; i<=10; i++){
            String card = Integer.toString(j);
                switch (i) {
                case 1:card += "♥️";
                break;
                case 2:card += "♦️";
                break;
                case 3:card += "♠️";
                break;
                case 4:card += "♣️";
                break;
                } 
                deck.add(card);
            } 
        }
    }
}
//♦️♥️♠️♣️