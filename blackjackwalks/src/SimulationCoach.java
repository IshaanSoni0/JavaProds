import org.knowm.xchart.*;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import java.util.List;
import java.util.ArrayList;

public class SimulationCoach {
    public static void main(String[] args) throws Exception {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.print("Number of decks in the shoe (e.g. 1,2,6): ");
        int numDecks = 1;
        try { numDecks = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception e) { numDecks = 1; }

        System.out.print("Starting player money: ");
        int startMoney = 1000;
        try { startMoney = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception e) { startMoney = 1000; }

        System.out.print("Number of games to simulate: ");
        int totalSteps = 1000;
        try { totalSteps = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception e) { totalSteps = 1000; }

        playBlackjack game = new playBlackjack();
        game.setMoney(startMoney);
        game.resetTrueCount();

        double[] steps       = new double[totalSteps];
        double[] moneySeries = new double[totalSteps];

        for (int i = 0; i < totalSteps; i++) {
            steps[i] = i + 1;

            // MATCHES BOT1: money <= 0 guard first
            if (game.getMoney() <= 0) {
                moneySeries[i] = game.getMoney();
                continue;
            }

            // MATCHES BOT1: resetgame() first, then deck check
            game.resetgame();
            if (game.deck.getlength() < 10) {
                game.resetDeck();
                game.resetTrueCount();
            }

            // MATCHES BOT1: bet uses raw getTrueCount() with no division
            int rc = game.getTrueCount();
            int bet;
            if      (rc >= 3) bet = (int)(0.10 * game.getMoney());
            else if (rc >= 2) bet = (int)(0.05 * game.getMoney());
            else              bet = 5;
            game.setBet(bet);

            // MATCHES BOT1: deal order and count update order exactly
            game.dealPlayer();
            game.updateTrueCount(game.getPlayerHand().get(game.getPlayerHand().size() - 1));
            game.dealDealer();
            game.updateTrueCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
            game.dealPlayer();
            game.updateTrueCount(game.getPlayerHand().get(game.getPlayerHand().size() - 1));
            game.dealDealer();
            // hole card dealt, NOT counted yet — counted after player actions, same as Bot1

            List<card> playerHand = new ArrayList<>(game.getPlayerHand());
            card dealerUpCard = game.getDealerCard(0);

            // ---------------------------------------------------------------
            // SPLIT CHECK — MATCHES BOT1's hardcoded switch exactly
            // Bot1 checks split at the top level before calling playHand
            // ---------------------------------------------------------------
            boolean didSplit = false;
            if (playerHand.size() == 2 && playerHand.get(0).getRank() == playerHand.get(1).getRank()) {
                int rank = playerHand.get(0).getRank();
                int dv   = dealerUpCard.getValue(); // raw card value (1=Ace, 2-10, face=10)
                String splitDecision = "NO_SPLIT";
                switch (rank) {
                    case 1:
                    case 8:
                        splitDecision = "SPLIT";
                        break;
                    case 9:
                        splitDecision = ((dv >= 2 && dv <= 6) || dv == 8 || dv == 9) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 7:
                        splitDecision = (dv >= 2 && dv <= 7) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 6:
                        splitDecision = (dv >= 2 && dv <= 6) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 4:
                        splitDecision = (dv == 5 || dv == 6) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 3:
                        splitDecision = (dv >= 4 && dv <= 7) ? "SPLIT" : "NO_SPLIT";
                        break;
                    case 2:
                        splitDecision = (dv >= 4 && dv <= 7) ? "SPLIT" : "NO_SPLIT";
                        break;
                    default:
                        splitDecision = "NO_SPLIT";
                }

                if ("SPLIT".equals(splitDecision)) {
                    didSplit = true;

                    // MATCHES BOT1: build split hands from deck
                    List<card> hand1 = new ArrayList<>();
                    hand1.add(playerHand.get(0));
                    if (game.deck.getlength() == 0) { game.resetDeck(); game.resetTrueCount(); }
                    card s1 = game.deck.getRandCard();
                    hand1.add(s1);
                    game.updateTrueCount(s1);

                    List<card> hand2 = new ArrayList<>();
                    hand2.add(playerHand.get(1));
                    if (game.deck.getlength() == 0) { game.resetDeck(); game.resetTrueCount(); }
                    card s2 = game.deck.getRandCard();
                    hand2.add(s2);
                    game.updateTrueCount(s2);

                    // MATCHES BOT1: play each hand using same decision logic
                    Boolean result1 = playHand(hand1, dealerUpCard, game);
                    Boolean result2 = playHand(hand2, dealerUpCard, game);

                    // MATCHES BOT1: count hole card, then dealer draws
                    game.updateTrueCount(game.getDealerHand().get(1));
                    while (!game.dealerBusted() && game.getDealerTotal() < 17) {
                        game.hitDealer();
                        // MATCHES BOT1: dealer hit cards NOT counted
                    }

                    // MATCHES BOT1 split resolution exactly:
                    // result==null (push) falls into else (-bet), same as Bot1's: if(r!=null&&r) +bet; else -bet
                    int b = game.getBet();
                    if (result1 != null && result1) game.setMoney(game.getMoney() + b);
                    else                            game.setMoney(game.getMoney() - b);

                    if (result2 != null && result2) game.setMoney(game.getMoney() + b);
                    else                            game.setMoney(game.getMoney() - b);
                }
            }

            // ---------------------------------------------------------------
            // NO SPLIT — MATCHES BOT1's playHand + resolve exactly
            // ---------------------------------------------------------------
            if (!didSplit) {
                Boolean result = playHand(playerHand, dealerUpCard, game);

                // MATCHES BOT1: count hole card, then dealer draws
                game.updateTrueCount(game.getDealerHand().get(1));
                while (!game.dealerBusted() && game.getDealerTotal() < 17) {
                    game.hitDealer();
                    // MATCHES BOT1: dealer hit cards NOT counted
                }

                // MATCHES BOT1 no-split resolution exactly
                int b = game.getBet();
                if      (result != null && result) game.setMoney(game.getMoney() + b);
                else if (result == null)            { /* push — no change */ }
                else                               game.setMoney(game.getMoney() - b);
            }

            moneySeries[i] = game.getMoney();
        }

        XYChart chart = new XYChartBuilder()
                .width(800).height(600)
                .title("Coach-perfect Simulation")
                .xAxisTitle("Games").yAxisTitle("Money")
                .build();
        chart.addSeries("Coach-perfect", steps, moneySeries);
        BitmapEncoder.saveBitmap(chart, "coach_sim", BitmapFormat.PNG);
        System.out.println("Simulation complete. Chart saved to coach_sim.png");
        scanner.close();
    }

    /**
     * Plays one hand to completion using Coach.decideAction, mirroring Bot1's playHand lambda exactly.
     *
     * KEY: win/loss comparison happens HERE against the dealer hand as it exists RIGHT NOW
     * (upcard + hole, before dealer draws) — this matches Bot1's lambda behavior exactly.
     * Bot1's lambda reads getDealerHand() before the dealer draw loop runs outside the lambda.
     *
     * Returns: true=win, false=loss, null=push
     */
    private static Boolean playHand(List<card> hand, card dealerUpCard, playBlackjack game) {
        List<card> currentHand = new ArrayList<>(hand);
        boolean handStand = false;

        while (!handStand) {
            int playerTotal = app.calcHandTotal(currentHand);
            int dealerValue = dealerUpCard.getValue() == 1 ? 11 : dealerUpCard.getValue();
            boolean isSoft  = app.isSoftHand(currentHand);
            boolean isPair  = app.isPairHand(currentHand);

            // MATCHES BOT1: rc = game.getTrueCount() (raw running count, no division)
            int rc = game.getTrueCount();

            // MATCHES BOT1: full basic strategy
            String action = "HIT";
            if (isPair) {
                int rank = currentHand.get(0).getRank();
                switch (rank) {
                    case 1:  action = "SPLIT"; break;
                    case 8:  action = "SPLIT"; break;
                    case 9:  action = ((dealerValue >= 2 && dealerValue <= 6) || dealerValue == 8 || dealerValue == 9) ? "SPLIT" : "STAND"; break;
                    case 7:  action = (dealerValue >= 2 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                    case 6:  action = (dealerValue >= 2 && dealerValue <= 6) ? "SPLIT" : "HIT"; break;
                    case 4:  action = (dealerValue == 5 || dealerValue == 6) ? "SPLIT" : "HIT"; break;
                    case 3:  action = (dealerValue >= 4 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                    case 2:  action = (dealerValue >= 4 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                    default: action = "STAND";
                }
            } else if (isSoft) {
                switch (playerTotal) {
                    case 20: action = "STAND"; break;
                    case 19: action = (dealerValue == 6) ? "DOUBLE" : "STAND"; break;
                    case 18:
                        if      (dealerValue >= 2 && dealerValue <= 6) action = "DOUBLE";
                        else if (dealerValue == 7 || dealerValue == 8)  action = "STAND";
                        else                                             action = "HIT";
                        break;
                    case 17: action = (dealerValue >= 3 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break;
                    case 16:
                    case 15: action = (dealerValue >= 4 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break;
                    case 14:
                    case 13: action = (dealerValue >= 5 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break;
                    default: action = "HIT";
                }
            } else {
                if      (playerTotal >= 17)                      action = "STAND";
                else if (playerTotal >= 13 && playerTotal <= 16) action = (dealerValue >= 2 && dealerValue <= 6) ? "STAND" : "HIT";
                else if (playerTotal == 12)                      action = (dealerValue >= 4 && dealerValue <= 6) ? "STAND" : "HIT";
                else if (playerTotal == 11)                      action = "DOUBLE";
                else if (playerTotal == 10)                      action = (dealerValue <= 9) ? "DOUBLE" : "HIT";
                else if (playerTotal == 9)                       action = (dealerValue >= 3 && dealerValue <= 6) ? "DOUBLE" : "HIT";
                else                                             action = "HIT";
            }

            // MATCHES BOT1: all Hi-Lo deviations using raw rc
            if (!isSoft && !isPair) {
                if (playerTotal == 16 && dealerValue == 9  && rc >= 5)  action = "STAND";
                if (playerTotal == 16 && dealerValue == 10 && rc >= 0)  action = "STAND";
                if (playerTotal == 16 && dealerValue == 11 && rc >= 3)  action = "STAND";
                if (playerTotal == 15 && dealerValue == 10 && rc >= 4)  action = "STAND";
                if (playerTotal == 15 && dealerValue == 9  && rc >= 5)  action = "STAND";
                if (playerTotal == 15 && dealerValue == 11 && rc >= 5)  action = "STAND";
                if (playerTotal == 14 && dealerValue == 10 && rc >= 3)  action = "STAND";
                if (playerTotal == 13 && dealerValue == 2  && rc <= -1) action = "HIT";
                if (playerTotal == 13 && dealerValue == 3  && rc <= -2) action = "HIT";
                if (playerTotal == 12 && dealerValue == 2  && rc >= 3)  action = "STAND";
                if (playerTotal == 12 && dealerValue == 3  && rc >= 2)  action = "STAND";
                if (playerTotal == 12 && dealerValue == 4  && rc < 0)   action = "HIT";
                if (playerTotal == 12 && dealerValue == 5  && rc < -2)  action = "HIT";
                if (playerTotal == 12 && dealerValue == 6  && rc < -1)  action = "HIT";
                if (playerTotal == 11 && dealerValue == 11 && rc >= 1)  action = "DOUBLE";
                if (playerTotal == 10 && dealerValue == 10 && rc >= 4)  action = "DOUBLE";
                if (playerTotal == 10 && dealerValue == 11 && rc >= 4)  action = "DOUBLE";
                if (playerTotal == 9  && dealerValue == 2  && rc >= 1)  action = "DOUBLE";
                if (playerTotal == 9  && dealerValue == 7  && rc >= 3)  action = "DOUBLE";
                if (playerTotal == 8  && dealerValue == 5  && rc >= 3)  action = "DOUBLE";
                if (playerTotal == 8  && dealerValue == 6  && rc >= 2)  action = "DOUBLE";
            }
            if (isSoft && !isPair) {
                if (playerTotal == 19 && dealerValue == 6  && rc >= 1)  action = "DOUBLE";
                if (playerTotal == 19 && dealerValue == 5  && rc >= 2)  action = "DOUBLE";
                if (playerTotal == 18 && dealerValue == 2  && rc >= 1)  action = "DOUBLE";
                if (playerTotal == 18 && dealerValue == 3  && rc <= -1) action = "STAND";
                if (playerTotal == 18 && dealerValue == 11 && rc >= 1)  action = "STAND";
                if (playerTotal == 17 && dealerValue == 2  && rc >= 1)  action = "DOUBLE";
                if (playerTotal == 17 && dealerValue == 3  && rc <= -1) action = "HIT";
                if (playerTotal == 15 && dealerValue == 4  && rc >= 0)  action = "DOUBLE";
                if (playerTotal == 14 && dealerValue == 4  && rc >= 1)  action = "DOUBLE";
                if (playerTotal == 13 && dealerValue == 5  && rc >= 0)  action = "DOUBLE";
            }
            if (isPair) {
                if (playerTotal == 20 && dealerValue == 5  && rc >= 5)  action = "SPLIT";
                if (playerTotal == 20 && dealerValue == 6  && rc >= 4)  action = "SPLIT";
                if (playerTotal == 18 && dealerValue == 7  && rc >= 3)  action = "SPLIT";
                if (playerTotal == 16 && dealerValue == 10 && rc >= 0)  action = "STAND";
                if (playerTotal == 8  && dealerValue == 4  && rc >= 5)  action = "SPLIT";
                if (playerTotal == 8  && dealerValue == 5  && rc >= 0)  action = "SPLIT";
                if (playerTotal == 8  && dealerValue == 6  && rc >= -1) action = "SPLIT";
            }

            // MATCHES BOT1: action execution
            switch (action) {
                case "HIT":
                    if (game.deck.getlength() == 0) { game.resetDeck(); game.resetTrueCount(); }
                    card newCard = game.deck.getRandCard();
                    currentHand.add(newCard);
                    game.updateTrueCount(newCard);
                    if (app.calcHandTotal(currentHand) > 21) handStand = true;
                    break;
                case "STAND":
                    handStand = true;
                    break;
                case "DOUBLE":
                    if (game.deck.getlength() == 0) { game.resetDeck(); game.resetTrueCount(); }
                    card doubleCard = game.deck.getRandCard();
                    currentHand.add(doubleCard);
                    game.updateTrueCount(doubleCard);
                    handStand = true;
                    break;
                case "SPLIT":
                    // MATCHES BOT1: split inside playHand returns null (treated as loss in split path)
                    return null;
            }
        }

        // MATCHES BOT1: compare against dealer hand RIGHT NOW (before dealer draws)
        // At this point dealer hand = upcard + hole card only (2 cards)
        int finalTotal  = app.calcHandTotal(currentHand);
        int dealerTotal = app.calcHandTotal(game.getDealerHand());

        if (finalTotal  > 21) return false;
        if (dealerTotal > 21) return true;
        if (finalTotal  > dealerTotal) return true;
        if (finalTotal == dealerTotal) return null;
        return false;
    }
}

//javac -cp "lib/xchart-3.8.8.jar;." -d bin src\*.java
//java -cp "bin;lib/xchart-3.8.8.jar" SimulationCoach