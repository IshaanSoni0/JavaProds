import java.util.List;
import java.util.ArrayList;

public class Bot1 {

    public double[] simulate(int totalSteps) {
        return simulateWithBetProfile(totalSteps, BetProfile.BALANCED);
    }

    public double[] simulateWithBetProfile(int totalSteps, int betProfile) {
        double[] player2money = new double[totalSteps];

        playBlackjack game = new playBlackjack();
        game.setMoney(5000);
        game.resetRunningCount();

        // ---- EDGE MEASUREMENT TRACKING ----
        int[]    handsAtRc  = new int[8];    // index = rc level (0=rc<0, 1=rc0, 2=rc1, etc)
        double[] totalReturnAtRc = new double[8]; // sum of (result/bet) at each rc level

        int previousBet = 5;
        for (int i = 0; i < totalSteps; i++) {

            if (game.getMoney() <= 0) {
                player2money[i] = game.getMoney();
                continue;
            }

            game.resetgame();
            if (game.deck.getlength() < 10) {
                game.resetDeck();
                game.resetRunningCount();
            }

            final int MIN_BET = 5;

            int rc = game.getRunningCount();
            int baseBet;

            switch (betProfile) {

                case BetProfile.CONSERVATIVE:
                    if      (rc >= 4) baseBet = 15;
                    else if (rc >= 3) baseBet = 10;
                    else if (rc >= 2) baseBet = 8;
                    else              baseBet = MIN_BET;
                    break;

                case BetProfile.FLAT:
                    baseBet = MIN_BET;
                    break;

                case BetProfile.AGGRESSIVE:
                    if      (rc >= 4) baseBet = 25;
                    else if (rc >= 3) baseBet = 20;
                    else if (rc >= 2) baseBet = 12;
                    else              baseBet = MIN_BET;
                    break;

                case BetProfile.RISKY:
                    if      (rc >= 5) baseBet = 50;
                    else if (rc >= 4) baseBet = 35;
                    else if (rc >= 3) baseBet = 25;
                    else if (rc >= 2) baseBet = 15;
                    else              baseBet = MIN_BET;
                    break;

                case BetProfile.PERCENT:
                    if      (rc >= 4) baseBet = (int)(0.01  * game.getMoney());
                    else if (rc >= 3) baseBet = (int)(0.006 * game.getMoney());
                    else if (rc >= 2) baseBet = (int)(0.002 * game.getMoney());
                    else              baseBet = MIN_BET;
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.RISKY_PERCENT:
                    if      (rc >= 5) baseBet = (int)(0.02  * game.getMoney());
                    else if (rc >= 4) baseBet = (int)(0.015 * game.getMoney());
                    else if (rc >= 3) baseBet = (int)(0.01  * game.getMoney());
                    else if (rc >= 2) baseBet = (int)(0.005 * game.getMoney());
                    else              baseBet = MIN_BET;
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.CONSERVATIVE_PERCENT:
                    if      (rc >= 4) baseBet = (int)(0.005 * game.getMoney());
                    else if (rc >= 3) baseBet = (int)(0.003 * game.getMoney());
                    else if (rc >= 2) baseBet = (int)(0.001 * game.getMoney());
                    else              baseBet = MIN_BET;
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.KELLY:
                    // Edge values measured empirically via MeasureEdge (1M hands)
                    double edgeKelly;
                    if      (rc >= 5) edgeKelly = 0.05449;
                    else if (rc >= 4) edgeKelly = 0.05046;
                    else if (rc >= 3) edgeKelly = 0.03780;
                    else if (rc >= 2) edgeKelly = 0.02361;
                    else if (rc >= 1) edgeKelly = 0.01044;
                    else              edgeKelly = 0.0;
                    if (edgeKelly > 0) {
                        baseBet = (int)((edgeKelly / 1.3) * game.getMoney());
                    } else {
                        baseBet = MIN_BET;
                    }
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.KELLY_HALF:
                    double edgeKellyHalf;
                    if      (rc >= 5) edgeKellyHalf = 0.05449;
                    else if (rc >= 4) edgeKellyHalf = 0.05046;
                    else if (rc >= 3) edgeKellyHalf = 0.03780;
                    else if (rc >= 2) edgeKellyHalf = 0.02361;
                    else if (rc >= 1) edgeKellyHalf = 0.01044;
                    else              edgeKellyHalf = 0.0;
                    if (edgeKellyHalf > 0) {
                        baseBet = (int)((edgeKellyHalf / 1.3) * 0.5 * game.getMoney());
                    } else {
                        baseBet = MIN_BET;
                    }
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.KELLY_QUARTER:
                    double edgeKellyQuarter;
                    if      (rc >= 5) edgeKellyQuarter = 0.05449;
                    else if (rc >= 4) edgeKellyQuarter = 0.05046;
                    else if (rc >= 3) edgeKellyQuarter = 0.03780;
                    else if (rc >= 2) edgeKellyQuarter = 0.02361;
                    else if (rc >= 1) edgeKellyQuarter = 0.01044;
                    else              edgeKellyQuarter = 0.0;
                    if (edgeKellyQuarter > 0) {
                        baseBet = (int)((edgeKellyQuarter / 1.3) * 0.25 * game.getMoney());
                    } else {
                        baseBet = MIN_BET;
                    }
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.KELLY_THREE_QUARTER:
                    double edgeKelly3Q;
                    if      (rc >= 5) edgeKelly3Q = 0.05449;
                    else if (rc >= 4) edgeKelly3Q = 0.05046;
                    else if (rc >= 3) edgeKelly3Q = 0.03780;
                    else if (rc >= 2) edgeKelly3Q = 0.02361;
                    else if (rc >= 1) edgeKelly3Q = 0.01044;
                    else              edgeKelly3Q = 0.0;
                    if (edgeKelly3Q > 0) {
                        baseBet = (int)((edgeKelly3Q / 1.3) * 0.75 * game.getMoney());
                    } else {
                        baseBet = MIN_BET;
                    }
                    if (baseBet < MIN_BET) baseBet = MIN_BET;
                    break;

                case BetProfile.BALANCED:
                default:
                    if      (rc >= 4) baseBet = 25;
                    else if (rc >= 3) baseBet = 20;
                    else if (rc >= 2) baseBet = 10;
                    else              baseBet = MIN_BET;
                    break;
            }

            game.setBet(baseBet);

            // ---- DEAL ----
            game.dealPlayer();
            game.updateRunningCount(game.getPlayerHand().get(game.getPlayerHand().size() - 1));
            game.dealDealer();
            game.updateRunningCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
            game.dealPlayer();
            game.updateRunningCount(game.getPlayerHand().get(game.getPlayerHand().size() - 1));
            game.dealDealer(); // hole card — not counted yet

            List<card> playerHand = new ArrayList<>(game.getPlayerHand());
            card dealerUpCard     = game.getDealerCard(0);

            // ---- BLACKJACK CHECK ----
            boolean playerBJ = app.isBlackjack(playerHand);
            boolean dealerBJ = app.isBlackjack(game.getDealerHand());
            game.updateRunningCount(game.getDealerHand().get(1));

            if (playerBJ || dealerBJ) {
                int rcIdx = Math.max(0, Math.min(rc + 2, 7));
                handsAtRc[rcIdx]++;
                if (playerBJ && dealerBJ) {
                    /* push — 0 return */
                } else if (playerBJ) {
                    game.setMoney(game.getMoney() + (baseBet + baseBet / 2));
                    totalReturnAtRc[rcIdx] += 1.5; // 3:2 payout = +1.5 per initial bet
                } else {
                    game.setMoney(game.getMoney() - baseBet);
                    totalReturnAtRc[rcIdx] -= 1.0;
                }
                player2money[i] = game.getMoney();
                continue;
            }

            // ---- SPLIT CHECK ----
            boolean didSplit = false;
            if (playerHand.size() == 2
                    && playerHand.get(0).getRank() == playerHand.get(1).getRank()) {

                int    rank  = playerHand.get(0).getRank();
                int    dv    = dealerUpCard.getValue();
                String split = "NO_SPLIT";
                switch (rank) {
                    case 1: case 8: split = "SPLIT"; break;
                    case 9: split = ((dv >= 2 && dv <= 6) || dv == 8 || dv == 9) ? "SPLIT" : "NO_SPLIT"; break;
                    case 7: split = (dv >= 2 && dv <= 7) ? "SPLIT" : "NO_SPLIT"; break;
                    case 6: split = (dv >= 2 && dv <= 6) ? "SPLIT" : "NO_SPLIT"; break;
                    case 4: split = (dv == 5 || dv == 6)  ? "SPLIT" : "NO_SPLIT"; break;
                    case 3: split = (dv >= 4 && dv <= 7)  ? "SPLIT" : "NO_SPLIT"; break;
                    case 2: split = (dv >= 4 && dv <= 7)  ? "SPLIT" : "NO_SPLIT"; break;
                    default: break;
                }

                if ("SPLIT".equals(split)) {
                    didSplit = true;

                    List<card> hand1 = new ArrayList<>();
                    hand1.add(playerHand.get(0));
                    if (game.deck.getlength() == 0) { game.resetDeck(); game.resetRunningCount(); }
                    card s1 = game.deck.getRandCard();
                    hand1.add(s1);
                    game.updateRunningCount(s1);

                    List<card> hand2 = new ArrayList<>();
                    hand2.add(playerHand.get(1));
                    if (game.deck.getlength() == 0) { game.resetDeck(); game.resetRunningCount(); }
                    card s2 = game.deck.getRandCard();
                    hand2.add(s2);
                    game.updateRunningCount(s2);

                    game.setBet(baseBet);
                    int total1 = playHand(hand1, dealerUpCard, game);
                    int bet1   = game.getBet();

                    game.setBet(baseBet);
                    int total2 = playHand(hand2, dealerUpCard, game);
                    int bet2   = game.getBet();

                    while (game.getDealerTotal() < 17) {
                        game.hitDealer();
                        game.updateRunningCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
                    }
                    int dealerTotal = game.getDealerTotal();

                    int result1 = resolveHand(total1, dealerTotal, bet1);
                    int result2 = resolveHand(total2, dealerTotal, bet2);
                    game.setMoney(game.getMoney() + result1 + result2);

                    // Track edge measurement for split hands
                    int rcIdx = Math.max(0, Math.min(rc + 2, 7));
                    handsAtRc[rcIdx]      += 2;
                    totalReturnAtRc[rcIdx] += (double) result1 / baseBet;
                    totalReturnAtRc[rcIdx] += (double) result2 / baseBet;
                }
            }

            // ---- NO SPLIT ----
            if (!didSplit) {
                game.setBet(baseBet);
                int playerTotal = playHand(playerHand, dealerUpCard, game);
                int finalBet    = game.getBet();

                while (game.getDealerTotal() < 17) {
                    game.hitDealer();
                    game.updateRunningCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
                }
                int dealerTotal = game.getDealerTotal();

                int result = resolveHand(playerTotal, dealerTotal, finalBet);
                game.setMoney(game.getMoney() + result);

                // Track edge measurement
                int rcIdx = Math.max(0, Math.min(rc + 2, 7));
                handsAtRc[rcIdx]++;
                totalReturnAtRc[rcIdx] += (double) result / baseBet;
            }

            player2money[i] = game.getMoney();
        }

        // ---- PRINT MEASURED EDGE PER COUNT LEVEL ----
        // Only print when running BALANCED so it doesn't spam for every bot
        if (betProfile == BetProfile.BALANCED) {
            System.out.println("\n=== Measured edge per count level (Bot1 BALANCED) ===");
            System.out.printf("%-10s %-10s %-12s %-12s%n", "rc level", "hands", "avg return", "edge %");
            System.out.println("--------------------------------------------------");
            String[] labels = {"rc<=-2", "rc=-1", "rc=0", "rc=1", "rc=2", "rc=3", "rc=4", "rc>=5"};
            for (int j = 0; j < 8; j++) {
                if (handsAtRc[j] > 0) {
                    double avgReturn = totalReturnAtRc[j] / handsAtRc[j];
                    System.out.printf("%-10s %-10d %-12.4f %-12s%n",
                        labels[j], handsAtRc[j],
                        avgReturn,
                        String.format("%.3f%%", avgReturn * 100));
                }
            }
            System.out.println();
            System.out.println("Kelly bet fractions derived from measured edges (edge / 1.2):");
            for (int j = 0; j < 8; j++) {
                if (handsAtRc[j] > 0) {
                    double avgReturn = totalReturnAtRc[j] / handsAtRc[j];
                    if (avgReturn > 0) {
                        System.out.printf("  %-10s  edge=%.3f%%  ->  bet %.4f%% of bankroll%n",
                            labels[j], avgReturn * 100, (avgReturn / 1.2) * 100);
                    }
                }
            }
            System.out.println();
        }

        return player2money;
    }

    static int playHand(List<card> hand, card dealerUpCard, playBlackjack game) {
        List<card> current = new ArrayList<>(hand);

        while (true) {
            int     playerTotal = app.calcHandTotal(current);
            int     dealerValue = dealerUpCard.getValue() == 1 ? 11 : dealerUpCard.getValue();
            boolean isSoft      = app.isSoftHand(current);
            boolean isPair      = app.isPairHand(current);
            boolean canDouble   = current.size() == 2;
            int     rc          = game.getRunningCount();

            String action = "HIT";

            if (isPair) {
                int rank = current.get(0).getRank();
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
                    case 19: action = (dealerValue >= 4 && dealerValue <= 6) ? "DOUBLE" : "STAND"; break; // single deck: double vs 4,5 also
                    case 18:
                        if      (dealerValue >= 2 && dealerValue <= 6) action = "DOUBLE";
                        else if (dealerValue == 7 || dealerValue == 8)  action = "STAND";
                        else                                             action = "HIT";
                        break;
                    case 17: action = (dealerValue >= 2 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break; // single deck: double vs 2 also
                    case 16:
                    case 15: action = (dealerValue >= 3 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break; // single deck: A,4/A,5 double vs 3-6
                    case 14:
                    case 13: action = (dealerValue >= 4 && dealerValue <= 6) ? "DOUBLE" : "HIT"; break; // single deck: A,2/A,3 double vs 4-6
                    default: action = "HIT"; break;
                }
                if (action.equals("DOUBLE") && !canDouble) action = "HIT";
            } else {
                if      (playerTotal >= 17)                      action = "STAND";
                else if (playerTotal >= 13 && playerTotal <= 16) action = (dealerValue >= 2 && dealerValue <= 6) ? "STAND" : "HIT";
                else if (playerTotal == 12)                      action = (dealerValue >= 4 && dealerValue <= 6) ? "STAND" : "HIT";
                else if (playerTotal == 11)                      action = "DOUBLE";
                else if (playerTotal == 10)                      action = (dealerValue <= 9) ? "DOUBLE" : "HIT";
                else if (playerTotal == 9)                       action = (dealerValue >= 2 && dealerValue <= 6) ? "DOUBLE" : "HIT"; // single deck: double vs 2 also
                else if (playerTotal == 8)                       action = (dealerValue == 5 || dealerValue == 6) ? "DOUBLE" : "HIT"; // single deck: double 8 vs 5,6
                else                                             action = "HIT";
                if (action.equals("DOUBLE") && !canDouble) action = "HIT";
            }

            if (!isSoft && !isPair) {
                if (playerTotal == 16 && dealerValue == 9  && rc >= 5)  action = "STAND";
                if (playerTotal == 16 && dealerValue == 10 && rc >= 4)  action = "STAND";
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
                // 9 vs 2: now in basic strategy (single deck), deviation removed
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
                if (playerTotal == 8  && dealerValue == 4  && rc >= 5)  action = "SPLIT";
                if (playerTotal == 8  && dealerValue == 5  && rc >= 0)  action = "SPLIT";
                if (playerTotal == 8  && dealerValue == 6  && rc >= -1) action = "SPLIT";
            }

            if (action.equals("STAND") || action.equals("SPLIT")) {
                break;
            } else if (action.equals("HIT")) {
                if (game.deck.getlength() == 0) { game.resetDeck(); game.resetRunningCount(); }
                card nc = game.deck.getRandCard();
                current.add(nc);
                game.updateRunningCount(nc);
                if (app.calcHandTotal(current) > 21) break;
            } else if (action.equals("DOUBLE")) {
                game.setBet(game.getBet() * 2);
                if (game.deck.getlength() == 0) { game.resetDeck(); game.resetRunningCount(); }
                card nc = game.deck.getRandCard();
                current.add(nc);
                game.updateRunningCount(nc);
                break;
            }
        }

        return app.calcHandTotal(current);
    }

    static int resolveHand(int playerTotal, int dealerTotal, int bet) {
        if (playerTotal > 21)           return -bet;
        if (dealerTotal > 21)           return +bet;
        if (playerTotal > dealerTotal)  return +bet;
        if (playerTotal == dealerTotal) return 0;
        return -bet;
    }
}