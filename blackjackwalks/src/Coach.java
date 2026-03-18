import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Coach {

    private static List<card> parseRanks(String line) {
        List<card> hand = new ArrayList<>();
        line = line.trim();
        if (line.isEmpty()) return hand;
        String[] parts = line.split("\\s+");
        for (String p : parts) {
            try {
                int r = Integer.parseInt(p);
                if      (r == 11)           hand.add(new card(0, 1)); // Ace = internal rank 1
                else if (r >= 2 && r <= 10) hand.add(new card(0, r));
            } catch (NumberFormatException e) { /* ignore */ }
        }
        return hand;
    }

    /**
     * rc = raw running count from getTrueCount(), same as Bot1 uses directly.
     * DOUBLE actions are only valid on 2-card hands — caller must enforce this.
     */
    public static String decideAction(List<card> currentHand, card dealerUpCard, int rc) {
        int     playerTotal = app.calcHandTotal(currentHand);
        int     dealerValue = dealerUpCard.getValue() == 1 ? 11 : dealerUpCard.getValue();
        boolean isSoft      = app.isSoftHand(currentHand);
        boolean isPair      = app.isPairHand(currentHand);
        boolean canDouble   = currentHand.size() == 2; // FIX: double only on 2-card hands

        String action = "HIT";

        // ---- BASIC STRATEGY ----
        if (isPair) {
            int rank = currentHand.get(0).getRank();
            switch (rank) {
                case 1: case 8: action = "SPLIT"; break;
                case 9: action = ((dealerValue >= 2 && dealerValue <= 6) || dealerValue == 8 || dealerValue == 9) ? "SPLIT" : "STAND"; break;
                case 7: action = (dealerValue >= 2 && dealerValue <= 7) ? "SPLIT" : "HIT"; break;
                case 6: action = (dealerValue >= 2 && dealerValue <= 6) ? "SPLIT" : "HIT"; break;
                case 4: action = (dealerValue == 5 || dealerValue == 6)  ? "SPLIT" : "HIT"; break;
                case 3: action = (dealerValue >= 4 && dealerValue <= 7)  ? "SPLIT" : "HIT"; break;
                case 2: action = (dealerValue >= 4 && dealerValue <= 7)  ? "SPLIT" : "HIT"; break;
                default: action = "STAND"; break;
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
                default: action = "HIT"; break;
            }
            if (action.equals("DOUBLE") && !canDouble) action = "HIT";
        } else {
            if      (playerTotal >= 17)                      action = "STAND";
            else if (playerTotal >= 13 && playerTotal <= 16) action = (dealerValue >= 2 && dealerValue <= 6) ? "STAND" : "HIT";
            else if (playerTotal == 12)                      action = (dealerValue >= 4 && dealerValue <= 6) ? "STAND" : "HIT";
            else if (playerTotal == 11)                      action = "DOUBLE";
            else if (playerTotal == 10)                      action = (dealerValue <= 9) ? "DOUBLE" : "HIT";
            else if (playerTotal == 9)                       action = (dealerValue >= 3 && dealerValue <= 6) ? "DOUBLE" : "HIT";
            else                                             action = "HIT";
            if (action.equals("DOUBLE") && !canDouble) action = "HIT";
        }

        // ---- HI-LO DEVIATIONS (raw running count) ----
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
            if (action.equals("DOUBLE") && !canDouble) action = "HIT";
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
            if (action.equals("DOUBLE") && !canDouble) action = "HIT";
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

        return action;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Number of decks in the shoe (e.g. 1,2,6): ");
        int numDecks = 1;
        try { numDecks = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception e) { numDecks = 1; }

        System.out.print("Your current points/money: ");
        int points = 1000;
        try { points = Integer.parseInt(scanner.nextLine().trim()); } catch (Exception e) { points = 1000; }

        int rc = 0; // raw running count

        System.out.println("Coach started. Enter 'q' at any prompt to quit.");

        roundLoop:
        while (true) {
            System.out.println("\n--- New Round ---");

            // Bet sizing: 12:1 spread matching Bot1 BALANCED and SimulationCoach exactly
            // rc >= 3 -> 6%, rc >= 2 -> 3%, rc >= 1 -> 1%, else -> 0.5%, minimum 1
            int suggestedBet;
            if      (rc >= 3) suggestedBet = (int)(0.06 * points);
            else if (rc >= 2) suggestedBet = (int)(0.03 * points);
            else if (rc >= 1) suggestedBet = (int)(0.01 * points);
            else              suggestedBet = (int)(0.005 * points);
            if (suggestedBet < 1) suggestedBet = 1;

            System.out.println("Running count: " + rc
                    + "  True count: " + String.format("%.2f", (double) rc / Math.max(1, numDecks))
                    + "  Money: " + points);
            System.out.println("Suggested bet: " + suggestedBet);

            System.out.print("Dealer upcard (2-11, 11=Ace) or 'q': ");
            String dline = scanner.nextLine().trim();
            if (dline.equalsIgnoreCase("q")) break;
            List<card> dealer = parseRanks(dline);
            if (dealer.isEmpty()) { System.out.println("Invalid. Try again."); continue; }
            card dealerUp = dealer.get(0);

            System.out.print("Your hand (space-separated, e.g. '10 7' or '11 6'): ");
            String pline = scanner.nextLine().trim();
            if (pline.equalsIgnoreCase("q")) break;
            List<card> playerHand = parseRanks(pline);
            if (playerHand.isEmpty()) { System.out.println("Invalid. Try again."); continue; }

            // Count visible cards: both player cards + dealer upcard
            for (card c : playerHand) rc += c.hiLowValue(c);
            rc += dealerUp.hiLowValue(dealerUp);

            // FIX: check for player blackjack on initial 2-card hand
            if (app.isBlackjack(playerHand)) {
                System.out.println("Blackjack! Pays 3:2.");
                System.out.print("Did dealer also have blackjack? (y/n): ");
                String dBJ = scanner.nextLine().trim();
                if (dBJ.equalsIgnoreCase("q")) break;
                if (dBJ.equalsIgnoreCase("y")) {
                    System.out.println("Both blackjack -> push. +0");
                } else {
                    int bjPay = (int)(suggestedBet * 1.5);
                    points += bjPay;
                    System.out.println("Player blackjack wins -> +" + bjPay);
                }
                System.out.println("Money: " + points);
                System.out.print("Continue? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;
                continue;
            }

            boolean playerDone        = false;
            boolean didSplitThisRound = false;
            int finalPlayerTotal      = 0;
            int finalPlayerTotalHand1 = 0;
            int finalPlayerTotalHand2 = 0;
            boolean playerBustedFlag  = false;
            int betThisRound          = suggestedBet;

            while (!playerDone) {
                String recommended = decideAction(playerHand, dealerUp, rc);
                System.out.println("\nCoach recommends: " + recommended);
                System.out.print("Follow? (y/n): ");
                String follow = scanner.nextLine().trim();
                if (follow.equalsIgnoreCase("q")) break roundLoop;

                String action = recommended;
                if (!follow.equalsIgnoreCase("y")) {
                    System.out.print("Your action (HIT/STAND/DOUBLE/SPLIT): ");
                    String alt = scanner.nextLine().trim();
                    if (alt.equalsIgnoreCase("q")) break roundLoop;
                    action = (alt.equalsIgnoreCase("HIT") || alt.equalsIgnoreCase("STAND")
                            || alt.equalsIgnoreCase("DOUBLE") || alt.equalsIgnoreCase("SPLIT"))
                            ? alt.toUpperCase() : "STAND";
                }

                if (action.equals("HIT")) {
                    System.out.print("Card received (2-11): ");
                    String r = scanner.nextLine().trim();
                    if (r.equalsIgnoreCase("q")) break roundLoop;
                    List<card> drawn = parseRanks(r);
                    if (drawn.isEmpty()) { System.out.println("Invalid."); continue; }
                    card nc = drawn.get(0);
                    playerHand.add(nc);
                    rc += nc.hiLowValue(nc);
                    if (app.calcHandTotal(playerHand) > 21) {
                        System.out.println("Busted! Total: " + app.calcHandTotal(playerHand));
                        playerBustedFlag = true;
                        playerDone = true;
                    }

                } else if (action.equals("DOUBLE")) {
                    System.out.print("Card received on double (2-11): ");
                    String r = scanner.nextLine().trim();
                    if (r.equalsIgnoreCase("q")) break roundLoop;
                    List<card> drawn = parseRanks(r);
                    if (!drawn.isEmpty()) {
                        card nc = drawn.get(0);
                        playerHand.add(nc);
                        rc += nc.hiLowValue(nc);
                        if (app.calcHandTotal(playerHand) > 21) {
                            System.out.println("Busted on double! Total: " + app.calcHandTotal(playerHand));
                            playerBustedFlag = true;
                        }
                    }
                    betThisRound *= 2;
                    playerDone = true;

                } else if (action.equals("SPLIT")) {
                    didSplitThisRound = true;
                    System.out.print("Card for hand 1 (2-11): ");
                    String r1 = scanner.nextLine().trim();
                    if (r1.equalsIgnoreCase("q")) break roundLoop;
                    System.out.print("Card for hand 2 (2-11): ");
                    String r2 = scanner.nextLine().trim();
                    if (r2.equalsIgnoreCase("q")) break roundLoop;

                    List<card> hand1 = new ArrayList<>();
                    hand1.add(playerHand.get(0));
                    hand1.addAll(parseRanks(r1));
                    List<card> hand2 = new ArrayList<>();
                    hand2.add(playerHand.get(1));
                    hand2.addAll(parseRanks(r2));
                    if (hand1.size() > 1) rc += hand1.get(1).hiLowValue(hand1.get(1));
                    if (hand2.size() > 1) rc += hand2.get(1).hiLowValue(hand2.get(1));

                    // FIX: track each split hand's bet independently
                    int bet1 = suggestedBet;
                    int bet2 = suggestedBet;

                    System.out.println("--- Hand 1 ---");
                    while (true) {
                        String a1rec = decideAction(hand1, dealerUp, rc);
                        System.out.println("Coach recommends for hand 1: " + a1rec);
                        System.out.print("Follow? (y/n): ");
                        String f1 = scanner.nextLine().trim();
                        if (f1.equalsIgnoreCase("q")) break roundLoop;
                        String a1 = f1.equalsIgnoreCase("y") ? a1rec : "STAND";
                        if (!f1.equalsIgnoreCase("y")) {
                            System.out.print("Your action (HIT/STAND/DOUBLE): ");
                            String alt1 = scanner.nextLine().trim();
                            if (alt1.equalsIgnoreCase("q")) break roundLoop;
                            a1 = alt1.toUpperCase();
                        }
                        if (a1.equals("HIT")) {
                            System.out.print("Card for hand 1 (2-11): ");
                            String rr = scanner.nextLine().trim();
                            if (rr.equalsIgnoreCase("q")) break roundLoop;
                            List<card> d = parseRanks(rr);
                            if (!d.isEmpty()) { hand1.add(d.get(0)); rc += d.get(0).hiLowValue(d.get(0)); }
                            if (app.calcHandTotal(hand1) > 21) { System.out.println("Hand 1 busted."); break; }
                        } else if (a1.equals("DOUBLE")) {
                            System.out.print("Card for hand 1 double (2-11): ");
                            String rr = scanner.nextLine().trim();
                            if (rr.equalsIgnoreCase("q")) break roundLoop;
                            List<card> d = parseRanks(rr);
                            if (!d.isEmpty()) { hand1.add(d.get(0)); rc += d.get(0).hiLowValue(d.get(0)); }
                            bet1 *= 2; // FIX: only hand1's bet doubles
                            if (app.calcHandTotal(hand1) > 21) System.out.println("Hand 1 busted on double.");
                            break;
                        } else break;
                    }

                    System.out.println("--- Hand 2 ---");
                    while (true) {
                        String a2rec = decideAction(hand2, dealerUp, rc);
                        System.out.println("Coach recommends for hand 2: " + a2rec);
                        System.out.print("Follow? (y/n): ");
                        String f2 = scanner.nextLine().trim();
                        if (f2.equalsIgnoreCase("q")) break roundLoop;
                        String a2 = f2.equalsIgnoreCase("y") ? a2rec : "STAND";
                        if (!f2.equalsIgnoreCase("y")) {
                            System.out.print("Your action (HIT/STAND/DOUBLE): ");
                            String alt2 = scanner.nextLine().trim();
                            if (alt2.equalsIgnoreCase("q")) break roundLoop;
                            a2 = alt2.toUpperCase();
                        }
                        if (a2.equals("HIT")) {
                            System.out.print("Card for hand 2 (2-11): ");
                            String rr = scanner.nextLine().trim();
                            if (rr.equalsIgnoreCase("q")) break roundLoop;
                            List<card> d = parseRanks(rr);
                            if (!d.isEmpty()) { hand2.add(d.get(0)); rc += d.get(0).hiLowValue(d.get(0)); }
                            if (app.calcHandTotal(hand2) > 21) { System.out.println("Hand 2 busted."); break; }
                        } else if (a2.equals("DOUBLE")) {
                            System.out.print("Card for hand 2 double (2-11): ");
                            String rr = scanner.nextLine().trim();
                            if (rr.equalsIgnoreCase("q")) break roundLoop;
                            List<card> d = parseRanks(rr);
                            if (!d.isEmpty()) { hand2.add(d.get(0)); rc += d.get(0).hiLowValue(d.get(0)); }
                            bet2 *= 2; // FIX: only hand2's bet doubles
                            if (app.calcHandTotal(hand2) > 21) System.out.println("Hand 2 busted on double.");
                            break;
                        } else break;
                    }

                    finalPlayerTotalHand1 = app.calcHandTotal(hand1);
                    finalPlayerTotalHand2 = app.calcHandTotal(hand2);

                    // Dealer: get hole card, count it, draw until >= 17
                    List<card> dealerHand = new ArrayList<>();
                    dealerHand.add(dealerUp);
                    System.out.print("Dealer hole card (2-11) or 'n': ");
                    String holeInput = scanner.nextLine().trim();
                    if (holeInput.equalsIgnoreCase("q")) break roundLoop;
                    if (!holeInput.equalsIgnoreCase("n") && !holeInput.isEmpty()) {
                        List<card> hole = parseRanks(holeInput);
                        if (!hole.isEmpty()) { dealerHand.add(hole.get(0)); rc += hole.get(0).hiLowValue(hole.get(0)); }
                    }
                    System.out.println("Enter dealer draws one at a time. 's' when dealer stands.");
                    while (true) {
                        System.out.print("Dealer card (2-11) or 's': ");
                        String dr = scanner.nextLine().trim();
                        if (dr.equalsIgnoreCase("q")) break roundLoop;
                        if (dr.equalsIgnoreCase("s")) break;
                        List<card> ddraw = parseRanks(dr);
                        if (!ddraw.isEmpty()) {
                            card dc = ddraw.get(0);
                            dealerHand.add(dc);
                            rc += dc.hiLowValue(dc);
                            if (app.calcHandTotal(dealerHand) > 21) { System.out.println("Dealer busted!"); break; }
                        }
                    }
                    int dealerTotal = app.calcHandTotal(dealerHand);

                    // FIX: each hand resolved with its own bet, push = push
                    System.out.println("Dealer total: " + dealerTotal);
                    int d1 = resolveHand(finalPlayerTotalHand1, dealerTotal, bet1);
                    System.out.println("Hand 1 (" + finalPlayerTotalHand1 + "): " + formatResult(d1));
                    points += d1;
                    int d2 = resolveHand(finalPlayerTotalHand2, dealerTotal, bet2);
                    System.out.println("Hand 2 (" + finalPlayerTotalHand2 + "): " + formatResult(d2));
                    points += d2;

                    System.out.println("Running count: " + rc
                            + "  True count: " + String.format("%.2f", (double) rc / Math.max(1, numDecks))
                            + "  Money: " + points);
                    System.out.print("Continue? (y/n): ");
                    if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break roundLoop;
                    continue roundLoop;

                } else { // STAND
                    playerDone = true;
                }
            }

            if (didSplitThisRound) continue; // already handled above

            finalPlayerTotal = app.calcHandTotal(playerHand);

            // Dealer: get hole card, count it, draw until >= 17
            List<card> dealerHand = new ArrayList<>();
            dealerHand.add(dealerUp);
            System.out.print("Dealer hole card (2-11) or 'n': ");
            String holeInput = scanner.nextLine().trim();
            if (holeInput.equalsIgnoreCase("q")) break;
            if (!holeInput.equalsIgnoreCase("n") && !holeInput.isEmpty()) {
                List<card> hole = parseRanks(holeInput);
                if (!hole.isEmpty()) { dealerHand.add(hole.get(0)); rc += hole.get(0).hiLowValue(hole.get(0)); }
            }
            System.out.println("Enter dealer draws one at a time. 's' when dealer stands.");
            while (true) {
                System.out.print("Dealer card (2-11) or 's': ");
                String dr = scanner.nextLine().trim();
                if (dr.equalsIgnoreCase("q")) break roundLoop;
                if (dr.equalsIgnoreCase("s")) break;
                List<card> ddraw = parseRanks(dr);
                if (!ddraw.isEmpty()) {
                    card dc = ddraw.get(0);
                    dealerHand.add(dc);
                    rc += dc.hiLowValue(dc);
                    if (app.calcHandTotal(dealerHand) > 21) { System.out.println("Dealer busted!"); break; }
                }
            }
            int dealerTotal = app.calcHandTotal(dealerHand);

            // FIX: resolve after full dealer hand, push = push
            int delta = playerBustedFlag ? -betThisRound : resolveHand(finalPlayerTotal, dealerTotal, betThisRound);
            System.out.println("Dealer: " + dealerTotal + "  You: " + finalPlayerTotal
                    + "  " + formatResult(delta));
            points += delta;

            System.out.println("Running count: " + rc
                    + "  True count: " + String.format("%.2f", (double) rc / Math.max(1, numDecks))
                    + "  Money: " + points);

            System.out.print("Continue? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;
        }

        System.out.println("Coach done. Final money: " + points);
        scanner.close();
    }

    private static int resolveHand(int playerTotal, int dealerTotal, int bet) {
        if (playerTotal > 21)           return -bet;
        if (dealerTotal > 21)           return +bet;
        if (playerTotal > dealerTotal)  return +bet;
        if (playerTotal == dealerTotal) return 0;
        return -bet;
    }

    private static String formatResult(int delta) {
        if (delta > 0) return "WIN  +" + delta;
        if (delta < 0) return "LOSE " + delta;
        return "PUSH  0";
    }
}

//for chromebook: cd /workspaces/JavaProds/blackjackwalks && javac -cp lib/xchart-3.8.8.jar -d bin src/*.java 2>&1
//                cd /workspaces/JavaProds/blackjackwalks && java -cp bin:lib/xchart-3.8.8.jar Coach
//for windows:    javac -cp "lib/xchart-3.8.8.jar;." -d bin src\*.java
//                java -cp "bin;lib/xchart-3.8.8.jar" Coach