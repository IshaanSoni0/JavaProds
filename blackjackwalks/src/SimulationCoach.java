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

            if (game.getMoney() <= 0) {
                moneySeries[i] = game.getMoney();
                continue;
            }

            game.resetgame();
            if (game.deck.getlength() < 10) {
                game.resetDeck();
                game.resetTrueCount();
            }

            // ---- BET SIZING: 12:1 spread, matches Coach suggestion ----
            int rc = game.getTrueCount();
            int baseBet;
            if      (rc >= 3) baseBet = (int)(0.06 * game.getMoney());
            else if (rc >= 2) baseBet = (int)(0.03 * game.getMoney());
            else if (rc >= 1) baseBet = (int)(0.01 * game.getMoney());
            else              baseBet = (int)(0.005 * game.getMoney());
            if (baseBet < 1) baseBet = 1;
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
                moneySeries[i] = game.getMoney();
                continue;
            }

            // ---- SPLIT CHECK (uses Coach.decideAction) ----
            boolean didSplit = false;
            if (playerHand.size() == 2
                    && playerHand.get(0).getRank() == playerHand.get(1).getRank()) {

                String coachSplit = Coach.decideAction(playerHand, dealerUpCard, game.getTrueCount());

                if (coachSplit.equals("SPLIT")) {
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
                    int total1 = playHandWithCoach(hand1, dealerUpCard, game);
                    int bet1   = game.getBet();

                    game.setBet(baseBet);
                    int total2 = playHandWithCoach(hand2, dealerUpCard, game);
                    int bet2   = game.getBet();

                    while (game.getDealerTotal() < 17) {
                        game.hitDealer();
                        game.updateTrueCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
                    }
                    int dealerTotal = game.getDealerTotal();

                    game.setMoney(game.getMoney() + resolveHand(total1, dealerTotal, bet1));
                    game.setMoney(game.getMoney() + resolveHand(total2, dealerTotal, bet2));
                }
            }

            // ---- NO SPLIT ----
            if (!didSplit) {
                game.setBet(baseBet);
                int playerTotal = playHandWithCoach(playerHand, dealerUpCard, game);
                int finalBet    = game.getBet();

                while (game.getDealerTotal() < 17) {
                    game.hitDealer();
                    game.updateTrueCount(game.getDealerHand().get(game.getDealerHand().size() - 1));
                }
                int dealerTotal = game.getDealerTotal();

                game.setMoney(game.getMoney() + resolveHand(playerTotal, dealerTotal, finalBet));
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

    private static int playHandWithCoach(List<card> hand, card dealerUpCard, playBlackjack game) {
        List<card> current = new ArrayList<>(hand);

        while (true) {
            String action = Coach.decideAction(current, dealerUpCard, game.getTrueCount());

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

    private static int resolveHand(int playerTotal, int dealerTotal, int bet) {
        if (playerTotal > 21)           return -bet;
        if (dealerTotal > 21)           return +bet;
        if (playerTotal > dealerTotal)  return +bet;
        if (playerTotal == dealerTotal) return 0;
        return -bet;
    }
}

//javac -cp "lib/xchart-3.8.8.jar;." -d bin src\*.java
//java -cp "bin;lib/xchart-3.8.8.jar" SimulationCoach