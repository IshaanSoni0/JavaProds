import java.util.ArrayList; 
import java.util.Scanner;

public class casino {
    public static void main(String[] args) {
        boolean leaveCasino = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Casino!");
        while(!leaveCasino){
        System.out.println("You can play blackjack, or poker. what would you like to play?");
        String gameChoice = scanner.nextLine().toLowerCase();
        if (gameChoice.equals("blackjack")) {
            blackjack.playBlackJack();
        } 
        else if (gameChoice.equals("poker")) {
            //poker.playPoker();
        } 
        else {
            System.out.println("you left the casino");
            leaveCasino = true;
        }
        }

    scanner.close();
    }
}

//  use: javac -d bin src/*.java -------- to compile to bin
//  use: java -cp bin casino -------- to run the blackjack.class in bin
