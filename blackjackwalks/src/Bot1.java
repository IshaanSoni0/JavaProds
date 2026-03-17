import java.util.List;
import java.util.ArrayList;
import java.util.function.BiFunction;

public class Bot1 {

    public double[] simulate(int totalSteps) {
        return simulateWithBetProfile(totalSteps, BetProfile.BALANCED);
    }

    // New: configurable simulate method with bet profile
    public double[] simulateWithBetProfile(int totalSteps, int betProfile) {
        double[] player2money = new double[totalSteps];

        playBlackjack player2 = new playBlackjack();
        player2.setMoney(1000);
        player2.resetTrueCount();

        java.util.function.BiFunction<List<card>, card, Boolean> playHand = (hand, dealerUpCard) -> {
            List<card> currentHand = new ArrayList<>(hand);
            boolean handStand = false;

            while (!handStand) {
                int playerTotal = app.calcHandTotal(currentHand);
                int dealerValue = dealerUpCard.getValue() == 1 ? 11 : dealerUpCard.getValue();

                boolean isSoft = app.isSoftHand(currentHand);
                boolean isPair = app.isPairHand(currentHand);

                String action = "HIT";

                // BASIC STRATEGY (same logic as original)
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

                int rc = player2.getTrueCount();

                // HI-LO DEVIATIONS (same as original)
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

                switch (action) {
                    case "HIT":
                        if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();}
                        card newCard = player2.deck.getRandCard();
                        currentHand.add(newCard);
                        player2.updateTrueCount(newCard);
                        if (app.calcHandTotal(currentHand) > 21) {
                            handStand = true;
                        }
                        break;
                    case "STAND":
                        handStand = true;
                        break;
                    case "DOUBLE":
                        if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();}
                        card doubleCard = player2.deck.getRandCard();
                        currentHand.add(doubleCard);
                        player2.updateTrueCount(doubleCard);
                        handStand = true;
                        break;
                    case "SPLIT":
                        return null;
                }
            }

            int finalTotal = app.calcHandTotal(currentHand);
            int dealerTotal = app.calcHandTotal(player2.getDealerHand());

            if (finalTotal > 21) return false;
            if (dealerTotal > 21) return true;
            if (finalTotal > dealerTotal) return true;
            if (finalTotal == dealerTotal) return null;
            return false;
        };

        for (int i = 0; i < totalSteps; i++) {
            if (player2.getMoney() <= 0) {
                player2money[i] = player2.getMoney();
                continue;
            }

            player2.resetgame();
            if (player2.deck.getlength() < 10) {
                player2.resetDeck();
                player2.resetTrueCount();
            }

            // Set bet based on true count and requested profile
            int rc = player2.getTrueCount();
            switch (betProfile) {
                case BetProfile.AGGRESSIVE:
                    if (rc >= 3) player2.setBet((int)(0.15 * player2.getMoney()));
                    else if (rc >= 2) player2.setBet((int)(0.10 * player2.getMoney()));
                    else player2.setBet(10);
                    break;
                case BetProfile.CONSERVATIVE:
                    if (rc >= 3) player2.setBet((int)(0.05 * player2.getMoney()));
                    else if (rc >= 2) player2.setBet((int)(0.03 * player2.getMoney()));
                    else player2.setBet(5);
                    break;
                case BetProfile.FLAT:
                    player2.setBet(10);
                    break;
                case BetProfile.RISKY:
                    if (rc >= 3) player2.setBet((int)(0.20 * player2.getMoney()));
                    else if (rc >= 2) player2.setBet((int)(0.10 * player2.getMoney()));
                    else player2.setBet(5);
                    break;
                case BetProfile.BALANCED:
                default:
                    if (rc >= 3) player2.setBet((int)(0.10 * player2.getMoney()));
                    else if (rc >= 2) player2.setBet((int)(0.05 * player2.getMoney()));
                    else player2.setBet(5);
                    break;
            }

            player2.dealPlayer();
            player2.updateTrueCount(player2.getPlayerHand().get(player2.getPlayerHand().size() - 1));
            player2.dealDealer();
            player2.updateTrueCount(player2.getDealerHand().get(player2.getDealerHand().size() - 1));
            player2.dealPlayer();
            player2.updateTrueCount(player2.getPlayerHand().get(player2.getPlayerHand().size() - 1));
            player2.dealDealer();

            List<card> playerHand = new ArrayList<>(player2.getPlayerHand());
            card dealerUpCard = player2.getDealerCard(0);

            boolean didSplit = false;
            if (playerHand.size() == 2 && playerHand.get(0).getRank() == playerHand.get(1).getRank()) {
                int rank = playerHand.get(0).getRank();
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
                    List<card> hand1 = new ArrayList<>();
                    hand1.add(playerHand.get(0));
                    if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();}
                    hand1.add(player2.deck.getRandCard());
                    player2.updateTrueCount(hand1.get(1));

                    List<card> hand2 = new ArrayList<>();
                    hand2.add(playerHand.get(1));
                    if(player2.deck.getlength() == 0) {player2.resetDeck(); player2.resetTrueCount();}
                    hand2.add(player2.deck.getRandCard());
                    player2.updateTrueCount(hand2.get(1));

                    Boolean result1 = playHand.apply(hand1, dealerUpCard);
                    Boolean result2 = playHand.apply(hand2, dealerUpCard);

                    player2.updateTrueCount(player2.getDealerHand().get(1));
                    while (!player2.dealerBusted() && player2.getDealerTotal() < 17) {
                        player2.hitDealer();
                    }

                    int bet = player2.getBet();
                    if (result1 != null && result1) player2.setMoney(player2.getMoney() + bet);
                    else player2.setMoney(player2.getMoney() - bet);

                    if (result2 != null && result2) player2.setMoney(player2.getMoney() + bet);
                    else player2.setMoney(player2.getMoney() - bet);
                }
            }

            if (!didSplit) {
                Boolean result = playHand.apply(playerHand, dealerUpCard);
                player2.updateTrueCount(player2.getDealerHand().get(1));
                while (!player2.dealerBusted() && player2.getDealerTotal() < 17) {
                    player2.hitDealer();
                }

                int bet = player2.getBet();
                if (result != null && result) {
                    player2.setMoney(player2.getMoney() + bet);
                } else if (result == null) {
                    // push
                } else {
                    player2.setMoney(player2.getMoney() - bet);
                }
            }

            player2money[i] = player2.getMoney();
        }

        return player2money;
    }
}
