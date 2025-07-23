public class card {
    String Csuit;
    int Cvalue;
    public card(int x, int value){
        if(x==0){Csuit = "hearts";}
        if(x==1){Csuit = "dimonds";}
        if(x==2){Csuit = "spades";}
        if(x==3){Csuit = "clubs";}
        Cvalue = value;
    }
    //♦️♥️♠️♣️
    public String getSuit(){
        return Csuit;
    }
    public int getValue(){
        if(Cvalue > 10){
            return 10; // Face cards are worth 10
        }
        return Cvalue;
    }
    public String toString(){
        String valueString;
        if(Cvalue == 1) {
            valueString = "Ace";
        } else if(Cvalue == 11) {
            valueString = "Jack";
        } else if(Cvalue == 12) {
            valueString = "Queen";
        } else if(Cvalue == 13) {
            valueString = "King";
        } else {
            valueString = Integer.toString(Cvalue);
        }
        return valueString + " of " + Csuit;
    }
}
