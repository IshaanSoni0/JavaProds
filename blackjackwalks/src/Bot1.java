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
        game.resetTrueCount();

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
                game.resetTrueCount();
            }

            final int MIN_BET = 5;

            int rc = game.getTrueCount();
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
                    int cappedRc = Math.min(rc, 4);
                    double edge = (cappedRc - 1) * 0.003;
                    double variance = 1.2;
                    if (edge > 0) {
                        baseBet = (int)((edge / variance) * game.getMoney());
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
            game.updateTrueCount(game.getPlayerHand().get(game.getPlayerHand().size() - 1));
            game.dealDealer();
            game.updateTrueCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
            game.dealPlayer();
            game.updateTrueCount(game.getPlayerHand().get(game.getPlayerHand().size() - 1));
            game.dealDealer(); // hole card — not counted yet

            List<card> playerHand = new ArrayList<>(game.getPlayerHand());
            card dealerUpCard     = game.getDealerCard(0);

            // ---- BLACKJACK CHECK ----
            boolean playerBJ = app.isBlackjack(playerHand);
            boolean dealerBJ = app.isBlackjack(game.getDealerHand());
            game.updateTrueCount(game.getDealerHand().get(1));

            if (playerBJ || dealerBJ) {
                if      (playerBJ && dealerBJ) { /* push */ }
                else if (playerBJ)             game.setMoney(game.getMoney() + (baseBet + baseBet / 2));
                else                           game.setMoney(game.getMoney() - baseBet);
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

                    game.setBet(baseBet);
                    int total1 = playHand(hand1, dealerUpCard, game);
                    int bet1   = game.getBet();

                    game.setBet(baseBet);
                    int total2 = playHand(hand2, dealerUpCard, game);
                    int bet2   = game.getBet();

                    while (game.getDealerTotal() < 17) {
                        game.hitDealer();
                        game.updateTrueCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
                    }
                    int dealerTotal = game.getDealerTotal();

                    int result1 = resolveHand(total1, dealerTotal, bet1);
                    int result2 = resolveHand(total2, dealerTotal, bet2);
                    game.setMoney(game.getMoney() + result1 + result2);

                    // Track edge measurement for split hands
                    int rcIdx = Math.max(0, Math.min(rc + 2, 7));
                    handsAtRc[rcIdx]      += 2;
                    totalReturnAtRc[rcIdx] += (double) result1 / bet1;
                    totalReturnAtRc[rcIdx] += (double) result2 / bet2;
                }
            }

            // ---- NO SPLIT ----
            if (!didSplit) {
                game.setBet(baseBet);
                int playerTotal = playHand(playerHand, dealerUpCard, game);
                int finalBet    = game.getBet();

                while (game.getDealerTotal() < 17) {
                    game.hitDealer();
                    game.updateTrueCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
                }
                int dealerTotal = game.getDealerTotal();

                int result = resolveHand(playerTotal, dealerTotal, finalBet);
                game.setMoney(game.getMoney() + result);

                // Track edge measurement
                int rcIdx = Math.max(0, Math.min(rc + 2, 7));
                handsAtRc[rcIdx]++;
                totalReturnAtRc[rcIdx] += (double) result / finalBet;
            }

            player2money[i] = game.getMoney();
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
            int     rc          = game.getTrueCount();

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

            if (!isSoft && !isPair) {
                if (playerTotal == 16 && dealerValue == 9  && rc >= 5)  action = "STAND";
                if (playerTotal == 16 && dealerValue == 10 && rc >= 4) action = "STAND";
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
                if (playerTotal == 8  && dealerValue == 4  && rc >= 5)  action = "SPLIT";
                if (playerTotal == 8  && dealerValue == 5  && rc >= 0)  action = "SPLIT";
                if (playerTotal == 8  && dealerValue == 6  && rc >= -1) action = "SPLIT";
            }

            if (action.equals("STAND") || action.equals("SPLIT")) {
                break;
            } else if (action.equals("HIT")) {
                if (game.deck.getlength() == 0) { game.resetDeck(); game.resetTrueCount(); }
                card nc = game.deck.getRandCard();
                current.add(nc);
                game.updateTrueCount(nc);
                if (app.calcHandTotal(current) > 21) break;
            } else if (action.equals("DOUBLE")) {
                game.setBet(game.getBet() * 2);
                if (game.deck.getlength() == 0) { game.resetDeck(); game.resetTrueCount(); }
                card nc = game.deck.getRandCard();
                current.add(nc);
                game.updateTrueCount(nc);
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