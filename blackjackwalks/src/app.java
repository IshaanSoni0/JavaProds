import org.knowm.xchart.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class app {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of games to simulate: ");
        int totalSteps = scanner.nextInt();

        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title("Blackjack: Basic Bot vs Hi-Lo Card Counter")
                .xAxisTitle("Games Played")
                .yAxisTitle("Money")
                .build();

        double[] steps = new double[totalSteps];
        double[] player1money = new double[totalSteps];
        double[] player2money = new double[totalSteps];

        playBlackjack player1 = new playBlackjack();
        playBlackjack player2 = new playBlackjack();

        player1.setMoney(1000);
        player2.setMoney(1000);

        // --- PLAYER 1: Basic bot (stand on 17, hit below 17) ---
        for (int i = 0; i < totalSteps; i++) {
            if (player1.getMoney() > 0) {
                player1.setBet(10);
                player1.resetgame();
                if (player1.deck.getlength() < 10) {
                    player1.resetDeck();
                }

                // Deal initial cards
                player1.dealPlayer();
                player1.dealDealer();
                player1.dealPlayer();
                player1.dealDealer();

                // Player hits < 17
                while (!player1.playerBusted() && player1.getPlayerTotal() < 17) {
                    player1.hitPlayer();
                }

                // Dealer hits < 17
                while (!player1.dealerBusted() && player1.getDealerTotal() < 17) {
                    player1.hitDealer();
                }

                // Determine winner
                boolean player1Win = false;
                if (player1.playerBusted()) player1Win = false;
                else if (player1.dealerBusted()) player1Win = true;
                else if (player1.getPlayerTotal() > player1.getDealerTotal()) player1Win = true;
                else if (player1.getPlayerTotal() == player1.getDealerTotal()) {
                    // Push, no money change
                } else player1Win = false;

                player1.loseWin(player1Win);

                steps[i] = i + 1;
                player1money[i] = player1.getMoney();
            } else {
                steps[i] = i + 1;
                player1money[i] = player1.getMoney();
            }
        }

        // --- PLAYER 2: Card counter with basic strategy + Hi-Lo deviations ---
        player2.resetTrueCount();

        // Add this helper method to play a single hand using basic strat + deviations
        // Returns boolean win/loss for that hand, also updates true count on hits
        java.util.function.BiFunction<List<card>, card, Boolean> playHand = (hand, dealerUpCard) -> {

            // We will simulate player moves on 'hand' with dealer's upcard known
            // hand = current player hand
            // dealerUpCard = dealer's visible card
            // We will need to apply basic strategy + deviations similarly to main code

            // Create a local copy of playerHand & dealerUpcard for decision making
            List<card> currentHand = new ArrayList<>(hand);
            boolean handStand = false;

            while (!handStand) {
                int playerTotal = calcHandTotal(currentHand);
                int dealerValue = dealerUpCard.getValue() == 1 ? 11 : dealerUpCard.getValue();

                boolean isSoft = isSoftHand(currentHand);
                boolean isPair = isPairHand(currentHand);

                String action = "HIT";

                // BASIC STRATEGY
                if (isPair) {
                    int rank = currentHand.get(0).getRank();
                    switch (rank) {
                        case 1: action = "SPLIT"; break; // AA
                        case 8: action = "SPLIT"; break; // 8,8
                        case 9: action = ((dealerValue >= 2 && dealerValue <= 6) || dealerValue == 8 || dealerValue == 9) ? "SPLIT" : "STAND"; break;
                        case 7: action = (dealerValue >= 2 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                        case 6: action = (dealerValue >= 2 && dealerValue <= 6) ? "SPLIT" : "HIT"; break;
                        case 4: action = (dealerValue == 5 || dealerValue == 6) ? "SPLIT" : "HIT"; break;
                        case 3: action = (dealerValue >= 4 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                        case 2: action = (dealerValue >= 4 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                        default: action = "STAND";
                    }
                } else if (isSoft) {
                    switch (playerTotal) {
                        case 20: action = "STAND"; break;
                        case 19: action = (dealerValue == 6) ? "DOUBLE" : "STAND"; break;
                        case 18:
                            if (dealerValue >= 2 && dealerValue <= 6) action = "DOUBLE";
                            else if (dealerValue == 7 || dealerValue == 8) action = "STAND";
                            else action = "HIT";
                            break;
                        case 17: action = (dealerValue >= 3 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break;
                        case 16:
                        case 15: action = (dealerValue >= 4 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break;
                        case 14:
                        case 13: action = (dealerValue >= 5 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break;
                        default: action = "HIT";
                    }
                } else {
                    // Hard totals
                    if (playerTotal >= 17) action = "STAND";
                    else if (playerTotal >= 13 && playerTotal <= 16) {
                        action = (dealerValue >= 2 && dealerValue <= 6) ? "STAND" : "HIT";
                    } else if (playerTotal == 12) {
                        action = (dealerValue >= 4 && dealerValue <= 6) ? "STAND" : "HIT";
                    } else if (playerTotal == 11) action = "DOUBLE";
                    else if (playerTotal == 10) action = (dealerValue <= 9) ? "DOUBLE" : "HIT";
                    else if (playerTotal == 9) action = (dealerValue >= 3 && dealerValue <= 6) ? "DOUBLE" : "HIT";
                    else action = "HIT";
                }

                // HI-LO DEVIATIONS
                int rc = player2.getTrueCount();

                if (!isSoft && !isPair) {
                    if (playerTotal == 16 && dealerValue == 9 && rc >= 5) action = "STAND";
                    if (playerTotal == 16 && dealerValue == 10 && rc >= 0) action = "STAND";
                    if (playerTotal == 16 && dealerValue == 11 && rc >= 3) action = "STAND";
                    if (playerTotal == 15 && dealerValue == 10 && rc >= 4) action = "STAND";
                    if (playerTotal == 15 && dealerValue == 9 && rc >= 5) action = "STAND";
                    if (playerTotal == 15 && dealerValue == 11 && rc >= 5) action = "STAND";
                    if (playerTotal == 14 && dealerValue == 10 && rc >= 3) action = "STAND";
                    if (playerTotal == 13 && dealerValue == 2 && rc <= -1) action = "HIT";
                    if (playerTotal == 13 && dealerValue == 3 && rc <= -2) action = "HIT";
                    if (playerTotal == 12 && dealerValue == 2 && rc >= 3) action = "STAND";
                    if (playerTotal == 12 && dealerValue == 3 && rc >= 2) action = "STAND";
                    if (playerTotal == 12 && dealerValue == 4 && rc < 0) action = "HIT";
                    if (playerTotal == 12 && dealerValue == 5 && rc < -2) action = "HIT";
                    if (playerTotal == 12 && dealerValue == 6 && rc < -1) action = "HIT";
                    if (playerTotal == 11 && dealerValue == 11 && rc >= 1) action = "DOUBLE";
                    if (playerTotal == 10 && dealerValue == 10 && rc >= 4) action = "DOUBLE";
                    if (playerTotal == 10 && dealerValue == 11 && rc >= 4) action = "DOUBLE";
                    if (playerTotal == 9 && dealerValue == 2 && rc >= 1) action = "DOUBLE";
                    if (playerTotal == 9 && dealerValue == 7 && rc >= 3) action = "DOUBLE";
                    if (playerTotal == 8 && dealerValue == 5 && rc >= 3) action = "DOUBLE";
                    if (playerTotal == 8 && dealerValue == 6 && rc >= 2) action = "DOUBLE";
                }
                if (isSoft && !isPair) {
                    if (playerTotal == 19 && dealerValue == 6 && rc >= 1) action = "DOUBLE";
                    if (playerTotal == 19 && dealerValue == 5 && rc >= 2) action = "DOUBLE";
                    if (playerTotal == 18 && dealerValue == 2 && rc >= 1) action = "DOUBLE";
                    if (playerTotal == 18 && dealerValue == 3 && rc <= -1) action = "STAND";
                    if (playerTotal == 18 && dealerValue == 11 && rc >= 1) action = "STAND";
                    if (playerTotal == 17 && dealerValue == 2 && rc >= 1) action = "DOUBLE";
                    if (playerTotal == 17 && dealerValue == 3 && rc <= -1) action = "HIT";
                    if (playerTotal == 15 && dealerValue == 4 && rc >= 0) action = "DOUBLE";
                    if (playerTotal == 14 && dealerValue == 4 && rc >= 1) action = "DOUBLE";
                    if (playerTotal == 13 && dealerValue == 5 && rc >= 0) action = "DOUBLE";
                }
                if (isPair) {
                    if (playerTotal == 20 && dealerValue == 5 && rc >= 5) action = "SPLIT";
                    if (playerTotal == 20 && dealerValue == 6 && rc >= 4) action = "SPLIT";
                    if (playerTotal == 18 && dealerValue == 7 && rc >= 3) action = "SPLIT";
                    if (playerTotal == 16 && dealerValue == 10 && rc >= 0) action = "STAND"; // 8,8 exception
                    if (playerTotal == 8 && dealerValue == 4 && rc >= 5) action = "SPLIT";
                    if (playerTotal == 8 && dealerValue == 5 && rc >= 0) action = "SPLIT";
                    if (playerTotal == 8 && dealerValue == 6 && rc >= -1) action = "SPLIT";
                }

                switch (action) {
                    case "HIT":

                    if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();} // Reset deck if empty

                        card newCard = player2.deck.getRandCard();
                        currentHand.add(newCard);
                        player2.updateTrueCount(newCard);
                        if (calcHandTotal(currentHand) > 21) {
                            handStand = true; // busted
                        }
                        break;
                    case "STAND":
                        handStand = true;
                        break;
                    case "DOUBLE":
                    if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();} // Reset deck if empty
                        // For simulation simplicity, double is one card and then stand
                        card doubleCard = player2.deck.getRandCard();
                        currentHand.add(doubleCard);
                        player2.updateTrueCount(doubleCard);
                        handStand = true;
                        // In a full simulation you'd double the bet here
                        break;
                    case "SPLIT":
                        // Return false, split should be handled outside
                        return null;
                }
            }

            // Return true if hand wins, false if loses (push is treated as loss here)
            int finalTotal = calcHandTotal(currentHand);
            int dealerTotal = calcHandTotal(player2.getDealerHand());

            if (finalTotal > 21) return false;
            if (dealerTotal > 21) return true;
            if (finalTotal > dealerTotal) return true;
            if (finalTotal == dealerTotal) return null; // push
            return false;
        };

        for (int i = 0; i < totalSteps; i++) {
            if (player2.getMoney() <= 0) {
                steps[i] = i + 1;
                player2money[i] = player2.getMoney();
                continue;
            }

            player2.resetgame();
            if (player2.deck.getlength() < 10) {
                player2.resetDeck();
                player2.resetTrueCount();
            }

            // Set bet based on true count
            if (player2.getTrueCount() >= 3) player2.setBet((int)(0.10 * player2.getMoney()));
            else if (player2.getTrueCount() >= 2) player2.setBet((int)(0.05 * player2.getMoney()));
            else player2.setBet(5);

            // Deal initial cards + update count
            player2.dealPlayer();
            player2.updateTrueCount(player2.getPlayerHand().get(player2.getPlayerHand().size() - 1));
            player2.dealDealer();
            player2.updateTrueCount(player2.getDealerHand().get(player2.getDealerHand().size() - 1));
            player2.dealPlayer();
            player2.updateTrueCount(player2.getPlayerHand().get(player2.getPlayerHand().size() - 1));
            player2.dealDealer();
            // Do NOT update true count for dealer hole card here until reveal

            // Check if first two cards are a pair for split
            List<card> playerHand = new ArrayList<>(player2.getPlayerHand());
            card dealerUpCard = player2.getDealerCard(0);

            // Handle splits if applicable (only one split allowed here)
            boolean didSplit = false;
            if (playerHand.size() == 2 &&
                    playerHand.get(0).getRank() == playerHand.get(1).getRank()) {
                int rank = playerHand.get(0).getRank();
                // Only split if basic strategy says to split for dealerUpCard and current true count
                // We'll use the same logic from above to decide if to split
                String splitDecision = "NO_SPLIT";

                switch (rank) {
                    case 1:
                    case 8:
                        splitDecision = "SPLIT";
                        break;
                    case 9:
                        if ((dealerUpCard.getValue() >= 2 && dealerUpCard.getValue() <= 6)
                                || dealerUpCard.getValue() == 8 || dealerUpCard.getValue() == 9)
                            splitDecision = "SPLIT";
                        else splitDecision = "NO_SPLIT";
                        break;
                    case 7:
                        splitDecision = (dealerUpCard.getValue() >= 2 && dealerUpCard.getValue() <= 7) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 6:
                        splitDecision = (dealerUpCard.getValue() >= 2 && dealerUpCard.getValue() <= 6) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 4:
                        splitDecision = (dealerUpCard.getValue() == 5 || dealerUpCard.getValue() == 6) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 3:
                        splitDecision = (dealerUpCard.getValue() >= 4 && dealerUpCard.getValue() <= 7) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 2:
                        splitDecision = (dealerUpCard.getValue() >= 4 && dealerUpCard.getValue() <= 7) ? "SPLIT" : "NO_SPLIT";
                        break;
                    default:
                        splitDecision = "NO_SPLIT";
                }

                if ("SPLIT".equals(splitDecision)) {
                    didSplit = true;

                    // Create two new hands
                    List<card> hand1 = new ArrayList<>();
                    hand1.add(playerHand.get(0));
                            if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();} // Reset deck if empty
                    hand1.add(player2.deck.getRandCard());
                    player2.updateTrueCount(hand1.get(1));

                    List<card> hand2 = new ArrayList<>();
                    hand2.add(playerHand.get(1));
                            if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();} // Reset deck if empty
                    hand2.add(player2.deck.getRandCard());
                    player2.updateTrueCount(hand2.get(1));

                    // Play both hands independently
                    Boolean result1 = playHand.apply(hand1, dealerUpCard);
                    Boolean result2 = playHand.apply(hand2, dealerUpCard);

                    // Dealer plays after player hands done
                    // Reveal dealer hole card now
                    player2.updateTrueCount(player2.getDealerHand().get(1));

                    while (!player2.dealerBusted() && player2.getDealerTotal() < 17) {
                        player2.hitDealer();
                    }

                    // Evaluate both hands and pay out
                    int bet = player2.getBet();
                    if (result1 != null && result1) player2.setMoney(player2.getMoney() + bet);
                    else player2.setMoney(player2.getMoney() - bet);

                    if (result2 != null && result2) player2.setMoney(player2.getMoney() + bet);
                    else player2.setMoney(player2.getMoney() - bet);

                }
            }

            if (!didSplit) {
                // No split, play normal hand

                // Play player hand with basic strat + deviations
                Boolean result = playHand.apply(playerHand, dealerUpCard);

                // Reveal dealer hole card now
                player2.updateTrueCount(player2.getDealerHand().get(1));

                // Dealer plays
                while (!player2.dealerBusted() && player2.getDealerTotal() < 17) {
                    player2.hitDealer();
                }

                // Evaluate outcome
                int bet = player2.getBet();
                if (result != null && result) {
                    player2.setMoney(player2.getMoney() + bet);
                } else if (result == null) {
                    // push, no money change
                } else {
                    player2.setMoney(player2.getMoney() - bet);
                }
            }

            steps[i] = i + 1;
            player2money[i] = player2.getMoney();
        }

        chart.addSeries("Basic Bot", steps, player1money);
        chart.addSeries("Card Counter", steps, player2money);
        new SwingWrapper<>(chart).displayChart();

        scanner.close();
    }

    // Helper to calculate hand total with soft Ace logic
    private static int calcHandTotal(List<card> hand) {
        int total = 0;
        int aceCount = 0;
        for (card c : hand) {
            total += c.getValue();
            if (c.getValue() == 1) aceCount++;
        }
        while (aceCount > 0 && total + 10 <= 21) {
            total += 10;
            aceCount--;
        }
        return total;
    }

    // Helper to detect soft hand (Ace counted as 11)
    private static boolean isSoftHand(List<card> hand) {
        int total = 0;
        int aceCount = 0;
        for (card c : hand) {
            total += c.getValue();
            if (c.getValue() == 1) aceCount++;
        }
        return aceCount > 0 && (total + 10) <= 21;
    }

    // Helper to detect pair (two cards same rank)
    private static boolean isPairHand(List<card> hand) {
        return hand.size() == 2 && hand.get(0).getRank() == hand.get(1).getRank();
    }
}


// use: javac -cp "lib/xchart-3.8.8.jar;." -d bin (Get-ChildItem src\*.java)    // to compile with XChart
// use: java -cp "lib/xchart-3.8.8.jar;bin" app    // to run the app
