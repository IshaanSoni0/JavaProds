import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Coach {

    // Small helper to parse space-separated ranks using 2-11 convention (2-10 numeric, 11 = Ace)
    // Maps input to internal card ranks where Ace is 1 and face cards/10 map to value 10 via card.getValue().
    private static List<card> parseRanks(String line) {
        List<card> hand = new ArrayList<>();
        line = line.trim();
        if (line.isEmpty()) return hand;
        String[] parts = line.split("\\s+");
        for (String p : parts) {
            try {
                int r = Integer.parseInt(p);
                // Accept 2..11, where 11 is Ace
                if (r == 11) {
                    hand.add(new card(0, 1)); // map 11 -> Ace (internal rank 1)
                } else if (r >= 2 && r <= 10) {
                    hand.add(new card(0, r));
                } else {
                    continue;
                }
            } catch (NumberFormatException e) {
                // ignore non-numeric tokens
            }
        }
        return hand;
    }

    // Apply the same decision logic used by Bot1 for a single decision
    private static String decideAction(List<card> currentHand, card dealerUpCard, int rc) {
        int playerTotal = app.calcHandTotal(currentHand);
        int dealerValue = dealerUpCard.getValue() == 1 ? 11 : dealerUpCard.getValue();
        boolean isSoft = app.isSoftHand(currentHand);
        boolean isPair = app.isPairHand(currentHand);

        String action = "HIT";
        if (isPair) {
            int rank = currentHand.get(0).getRank();
            switch (rank) {
                case 1: action = "SPLIT"; break;
                case 8: action = "SPLIT"; break;
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

        // Hi-Lo deviations (same as Bot1)
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
            if (playerTotal == 16 && dealerValue == 10 && rc >= 0) action = "STAND";
            if (playerTotal == 8 && dealerValue == 4 && rc >= 5) action = "SPLIT";
            if (playerTotal == 8 && dealerValue == 5 && rc >= 0) action = "SPLIT";
            if (playerTotal == 8 && dealerValue == 6 && rc >= -1) action = "SPLIT";
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

        int runningCount = 0;

        System.out.println("Coach started. Enter 'q' at any prompt to quit the coach.");

        roundLoop:
        while (true) {
            System.out.println("\n--- New Round ---");

            System.out.print("Enter dealer upcard rank (single rank 2-11, where 11 = Ace), or 'q' to quit: ");
            String dline = scanner.nextLine().trim();
            if (dline.equalsIgnoreCase("q")) break;
            List<card> dealer = parseRanks(dline);
            if (dealer.size() == 0) {
                System.out.println("No valid dealer upcard entered. Try again.");
                continue;
            }
            card dealerUp = dealer.get(0);

            System.out.print("Enter your current hand ranks (space-separated, e.g. '10 7' or '11 8'): ");
            String pline = scanner.nextLine().trim();
            if (pline.equalsIgnoreCase("q")) break;
            List<card> playerHand = parseRanks(pline);
            if (playerHand.size() == 0) {
                System.out.println("No valid player cards entered. Try again.");
                continue;
            }

            // Update running count for visible cards
            for (card c : playerHand) runningCount += c.hiLowValue(c);
            runningCount += dealerUp.hiLowValue(dealerUp);

            double trueCountRaw = (double) runningCount / Math.max(1, numDecks);
            int tc = (int) Math.round(trueCountRaw);

            // Suggest bet using Bot1 balanced logic
            int suggestedBet;
            if (tc >= 3) suggestedBet = Math.max(1, (int)(0.10 * points));
            else if (tc >= 2) suggestedBet = Math.max(1, (int)(0.05 * points));
            else suggestedBet = 5;

                System.out.println("Running count: " + runningCount + ", True count (approx): " + String.format("%.2f", trueCountRaw) + " (rounded " + tc + ")" + ", Player money: " + points);
                System.out.println("Suggested bet (balanced profile): " + suggestedBet);

            // Interactive player decision loop
            boolean playerDone = false;
            boolean didSplitThisRound = false;
            int finalPlayerTotal = 0;
            int finalPlayerTotalHand1 = 0;
            int finalPlayerTotalHand2 = 0;
            boolean playerBustedFlag = false;
            int betThisRound = suggestedBet;

            while (!playerDone) {
                String recommended = decideAction(playerHand, dealerUp, tc);
                System.out.println();
                System.out.println("Coach recommends: " + recommended);
                System.out.print("Do you follow the coach? (y/n): ");
                String follow = scanner.nextLine().trim();
                if (follow.equalsIgnoreCase("q")) break roundLoop;
                String action = recommended;
                if (!follow.equalsIgnoreCase("y")) {
                    System.out.print("What did you do instead? (HIT/STAND/DOUBLE/SPLIT): ");
                    String alt = scanner.nextLine().trim();
                    if (alt.equalsIgnoreCase("q")) break roundLoop;
                    if (alt.equalsIgnoreCase("HIT") || alt.equalsIgnoreCase("STAND") || alt.equalsIgnoreCase("DOUBLE") || alt.equalsIgnoreCase("SPLIT")) {
                        action = alt.toUpperCase();
                    } else {
                        System.out.println("Unrecognized action, assuming STAND.");
                        action = "STAND";
                    }
                }

                if (action.equals("HIT")) {
                    System.out.print("Enter rank of card you received (2-11, e.g. 10 or 11): ");
                    String r = scanner.nextLine().trim();
                    if (r.equalsIgnoreCase("q")) break roundLoop;
                    List<card> drawn = parseRanks(r);
                    if (drawn.size() == 0) { System.out.println("No valid card entered. Assuming no draw."); continue; }
                    card newC = drawn.get(0);
                    playerHand.add(newC);
                    runningCount += newC.hiLowValue(newC);
                    if (app.calcHandTotal(playerHand) > 21) {
                        System.out.println("You busted with total " + app.calcHandTotal(playerHand));
                        playerBustedFlag = true;
                        playerDone = true;
                        break;
                    }
                    // continue loop
                } else if (action.equals("DOUBLE")) {
                    System.out.print("Enter rank of card you received on double (2-11, e.g. 10 or 11): ");
                    String r = scanner.nextLine().trim();
                    if (r.equalsIgnoreCase("q")) break roundLoop;
                    List<card> drawn = parseRanks(r);
                    if (drawn.size() == 0) { System.out.println("No valid card entered."); }
                    else {
                        card newC = drawn.get(0);
                        playerHand.add(newC);
                        runningCount += newC.hiLowValue(newC);
                    }
                    betThisRound = betThisRound * 2; // double the bet for this hand
                    playerDone = true; // double then stand
                } else if (action.equals("SPLIT")) {
                    // perform split flow
                    didSplitThisRound = true;
                    System.out.println("Split requested. For coach, play each resulting hand separately.");
                    System.out.print("Enter card received for first split hand (rank 2-11, e.g. 10): ");
                    String r1 = scanner.nextLine().trim();
                    if (r1.equalsIgnoreCase("q")) break roundLoop;
                    System.out.print("Enter card received for second split hand (rank 2-11, e.g. 8): ");
                    String r2 = scanner.nextLine().trim();
                    if (r2.equalsIgnoreCase("q")) break roundLoop;
                    List<card> hand1 = new ArrayList<>();
                    hand1.add(playerHand.get(0));
                    hand1.addAll(parseRanks(r1));
                    List<card> hand2 = new ArrayList<>();
                    hand2.add(playerHand.get(1));
                    hand2.addAll(parseRanks(r2));
                    if (hand1.size() > 1) runningCount += hand1.get(1).hiLowValue(hand1.get(1));
                    if (hand2.size() > 1) runningCount += hand2.get(1).hiLowValue(hand2.get(1));
                    System.out.println("Now playing first split hand:");
                    // play hand1
                    while (true) {
                        String a1rec = decideAction(hand1, dealerUp, tc);
                        System.out.println("Coach recommends for hand1: " + a1rec);
                        System.out.print("Do you follow the coach for hand1? (y/n): ");
                        String f1 = scanner.nextLine().trim();
                        if (f1.equalsIgnoreCase("q")) break roundLoop;
                        String a1 = a1rec;
                        if (!f1.equalsIgnoreCase("y")) {
                            System.out.print("What did you do instead for hand1? (HIT/STAND): ");
                            String alt1 = scanner.nextLine().trim();
                            if (alt1.equalsIgnoreCase("q")) break roundLoop;
                            a1 = alt1.toUpperCase();
                        }
                        if (a1.equals("HIT")) {
                            System.out.print("Enter card you received for hand1 (rank 2-11, e.g. 5): ");
                            String rr = scanner.nextLine().trim();
                            if (rr.equalsIgnoreCase("q")) break roundLoop;
                            List<card> d = parseRanks(rr);
                            if (d.size() > 0) { hand1.add(d.get(0)); runningCount += d.get(0).hiLowValue(d.get(0)); }
                            if (app.calcHandTotal(hand1) > 21) { System.out.println("Hand1 busted."); break; }
                            continue;
                        }
                        break;
                    }
                    System.out.println("Now playing second split hand:");
                    while (true) {
                        String a2rec = decideAction(hand2, dealerUp, tc);
                        System.out.println("Coach recommends for hand2: " + a2rec);
                        System.out.print("Do you follow the coach for hand2? (y/n): ");
                        String f2 = scanner.nextLine().trim();
                        if (f2.equalsIgnoreCase("q")) break roundLoop;
                        String a2 = a2rec;
                        if (!f2.equalsIgnoreCase("y")) {
                            System.out.print("What did you do instead for hand2? (HIT/STAND): ");
                            String alt2 = scanner.nextLine().trim();
                            if (alt2.equalsIgnoreCase("q")) break roundLoop;
                            a2 = alt2.toUpperCase();
                        }
                        if (a2.equals("HIT")) {
                            System.out.print("Enter card you received for hand2 (rank 2-11, e.g. 7): ");
                            String rr = scanner.nextLine().trim();
                            if (rr.equalsIgnoreCase("q")) break roundLoop;
                            List<card> d = parseRanks(rr);
                            if (d.size() > 0) { hand2.add(d.get(0)); runningCount += d.get(0).hiLowValue(d.get(0)); }
                            if (app.calcHandTotal(hand2) > 21) { System.out.println("Hand2 busted."); break; }
                            continue;
                        }
                        break;
                    }
                    finalPlayerTotalHand1 = app.calcHandTotal(hand1);
                    finalPlayerTotalHand2 = app.calcHandTotal(hand2);
                    playerDone = true;
                } else if (action.equals("STAND")) {
                    playerDone = true;
                } else {
                    playerDone = true;
                }
            }

            // Before asking dealer draws, record final player total for non-split hands
            if (!didSplitThisRound) {
                finalPlayerTotal = app.calcHandTotal(playerHand);
            }

            // Dealer finalization: ask user only for additional dealer draws (do not re-enter upcard/hole)
            List<card> dealerHand = new ArrayList<>();
            dealerHand.add(dealerUp);
            System.out.println();
            System.out.println("Now enter any additional dealer draws (ranks 2-11) one at a time. Enter 's' when dealer stands.");
            while (true) {
                System.out.print("Dealer next card (2-11) or 's' when dealer stands: ");
                String dr = scanner.nextLine().trim();
                if (dr.equalsIgnoreCase("q")) break roundLoop;
                if (dr.equalsIgnoreCase("s")) break;
                List<card> ddraw = parseRanks(dr);
                if (ddraw.size() > 0) {
                    dealerHand.add(ddraw.get(0));
                    runningCount += ddraw.get(0).hiLowValue(ddraw.get(0));
                    if (app.calcHandTotal(dealerHand) > 21) { System.out.println("Dealer busted with " + app.calcHandTotal(dealerHand)); break; }
                } else {
                    System.out.println("No valid card entered.");
                }
            }

            int dealerTotal = app.calcHandTotal(dealerHand);

            // Resolve bets and update points
            if (didSplitThisRound) {
                int handBet = suggestedBet; // each split hand uses original suggested bet
                System.out.println("Resolving split hands vs dealer total " + dealerTotal + ":");
                // hand1
                System.out.println(" Hand1 total: " + finalPlayerTotalHand1);
                if (finalPlayerTotalHand1 > 21) {
                    System.out.println("  Hand1 busted -> lose " + handBet);
                    points -= handBet;
                } else if (dealerTotal > 21 || finalPlayerTotalHand1 > dealerTotal) {
                    System.out.println("  Hand1 wins -> +" + handBet);
                    points += handBet;
                } else if (finalPlayerTotalHand1 == dealerTotal) {
                    System.out.println("  Hand1 push -> +0");
                } else {
                    System.out.println("  Hand1 loses -> -" + handBet);
                    points -= handBet;
                }
                // hand2
                System.out.println(" Hand2 total: " + finalPlayerTotalHand2);
                if (finalPlayerTotalHand2 > 21) {
                    System.out.println("  Hand2 busted -> lose " + handBet);
                    points -= handBet;
                } else if (dealerTotal > 21 || finalPlayerTotalHand2 > dealerTotal) {
                    System.out.println("  Hand2 wins -> +" + handBet);
                    points += handBet;
                } else if (finalPlayerTotalHand2 == dealerTotal) {
                    System.out.println("  Hand2 push -> +0");
                } else {
                    System.out.println("  Hand2 loses -> -" + handBet);
                    points -= handBet;
                }
            } else {
                System.out.println("Resolving hand vs dealer total " + dealerTotal + ": Player total " + finalPlayerTotal);
                if (playerBustedFlag) {
                    System.out.println(" Player busted -> lose " + betThisRound);
                    points -= betThisRound;
                } else if (dealerTotal > 21 || finalPlayerTotal > dealerTotal) {
                    System.out.println(" Player wins -> +" + betThisRound);
                    points += betThisRound;
                } else if (finalPlayerTotal == dealerTotal) {
                    System.out.println(" Push -> +0");
                } else {
                    System.out.println(" Player loses -> -" + betThisRound);
                    points -= betThisRound;
                }
            }

            System.out.println("End of round. Running count now: " + runningCount + ", true count approx: " + String.format("%.2f", (double)runningCount / Math.max(1, numDecks)) + ", Player money: " + points);

            // Ask to continue
            System.out.print("Continue to next round? (y/n): ");
            String cont = scanner.nextLine().trim();
            if (!cont.equalsIgnoreCase("y")) break;
        }

        System.out.println("Coach exiting. Final running count: " + runningCount + ", Player money: " + points);
        scanner.close();
    }
}
