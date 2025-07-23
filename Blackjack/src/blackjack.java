import java.util.ArrayList; 
import java.util.Scanner;

public class blackjack {
    public static void playBlackJack(){
        int attackCount = 0; // Initialize attack count
        Scanner scanner = new Scanner(System.in);
        deck deck = new deck();
        ArrayList<card> playerHand;
        ArrayList<card> dealerHand;
        int playerMoney = 100; // Initialize player's money
        System.out.println("\n_______________________________________________________________________");
        System.out.println("Welcome to Blackjack!");

        while (playerMoney > 0) {
            System.out.println("You have $" + playerMoney + ". How much would you like to bet?");
            String betInput = scanner.nextLine();

            if (betInput.equalsIgnoreCase("attack")) {
                if(attackCount >= 5) {
                    System.out.println("you have attacked all the dealers in vegas!\nYou have been shot and killed by the gaurds!\nGame Over!");
                    break;
                }
                
                playerMoney += 50;
                System.out.println("you attack the dealer!\nYou steal $50 from the dealer and go to a different table!");
                attackCount++;
                continue;
            }

            int bet;
            try {
                bet = Integer.parseInt(betInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number");
                continue;
            }

            if (bet > playerMoney) {
                System.out.println("You cannot bet more than you have. Please try again.");
                continue;
            }

            playerHand = new ArrayList<card>();
            dealerHand = new ArrayList<card>();
            deck = new deck(); // Reset the deck for a new game

            playerHand.add(deck.getRandCard());
            playerHand.add(deck.getRandCard());
            System.out.println("Your hand: " + playerHand.get(0).toString() + ", and " + playerHand.get(1).toString());
            int playerTotal = playerHand.get(0).getValue() + playerHand.get(1).getValue();
            System.out.println("Your total value: " + playerTotal);
            System.out.println("_______________________________________________________________________");

            dealerHand.add(deck.getRandCard());
            dealerHand.add(deck.getRandCard());
            System.out.println("Dealer's hand: " + dealerHand.get(0).toString() + " (one card hidden)");
            int dealerTotal = dealerHand.get(0).getValue();
            System.out.println("Dealer's total value: " + dealerTotal);
            System.out.println("_______________________________________________________________________");

            boolean playerBusted = false;
            while(!playerBusted){
                System.out.println("Your total value is "+playerTotal+". Do you want to hit or stand? (h/s)");
                String choice = scanner.nextLine();
                if(choice.equalsIgnoreCase("h")){
                    card newCard = deck.getRandCard();
                    playerHand.add(newCard);
                    System.out.println("You drew: " + newCard.toString());
                    playerTotal += newCard.getValue();
                    System.out.println("Your total value: " + playerTotal);
                    if(playerTotal > 21){
                        System.out.println("You busted!");
                        playerBusted = true;
                    }
                } else if(choice.equalsIgnoreCase("s")){
                    break;
                } else {
                    System.out.println("Invalid choice, please enter 'h' or 's'.");
                }
            }

            if(!playerBusted){
                System.out.println("_______________________________________________________________________");
                System.out.println("Dealer's turn:");
                System.out.println("Dealer's hand: " + dealerHand.get(0).toString() + ", and " + dealerHand.get(1).toString());
                dealerTotal += dealerHand.get(1).getValue();
                System.out.println("Dealer's total value: " + dealerTotal);
                while(dealerTotal < 17){
                    card newCard = deck.getRandCard();
                    dealerHand.add(newCard);
                    System.out.println("Dealer drew: " + newCard.toString());
                    dealerTotal += newCard.getValue();
                }
                System.out.println("Dealer's total value: " + dealerTotal);
                System.out.println("_______________________________________________________________________");
                System.out.println("Final Results:");
                System.out.println("Your total value: " + playerTotal);
                System.out.println("Dealer's total value: " + dealerTotal);
                if(dealerTotal > 21 || playerTotal > dealerTotal){
                    System.out.println("You win!");
                    playerMoney += bet; // Add bet to player's money
                } else if(playerTotal < dealerTotal){
                    System.out.println("You lose!");
                    playerMoney -= bet; // Subtract bet from player's money
                } else {
                    System.out.println("It's a tie!");
                }
            } else {
                System.out.println("You lose!");
                playerMoney -= bet; // Subtract bet from player's money
            }
            //put cards back in the deck
            deck = new deck();
            System.out.println("_______________________________________________________________________");
        }
        if(attackCount < 5) {
            System.out.println("You have run out of money! Thank you for playing!");        }
        scanner.close();
    }
}
//  use: javac -d bin src/*.java -------- to compile to bin
//  use: java -cp bin blackjack -------- to run the blackjack.class in bin