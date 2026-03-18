import org.knowm.xchart.*;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.style.markers.SeriesMarkers;
import java.io.IOException;
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

        double[] steps        = new double[totalSteps];
        double[] player1money = new double[totalSteps];

        playBlackjack player1 = new playBlackjack();
        player1.setMoney(5000);

        // --- PLAYER 1: Basic bot (stand on 17, hit below 17) ---
        for (int i = 0; i < totalSteps; i++) {
            steps[i] = i + 1;

            if (player1.getMoney() <= 0) {
                player1money[i] = player1.getMoney();
                continue;
            }

            // Scaling bet: 1% of bankroll, minimum 1
            int bet = Math.max(1, (int)(0.01 * player1.getMoney()));
            player1.setBet(bet);

            player1.resetgame();
            if (player1.deck.getlength() < 10) {
                player1.resetDeck();
            }

            player1.dealPlayer();
            player1.dealDealer();
            player1.dealPlayer();
            player1.dealDealer();

            // Blackjack check
            boolean playerBJ = isBlackjack(player1.getPlayerHand());
            boolean dealerBJ = isBlackjack(player1.getDealerHand());

            if (playerBJ && dealerBJ) {
                // push
            } else if (playerBJ) {
                player1.setMoney(player1.getMoney() + (bet + bet / 2)); // 3:2
            } else if (dealerBJ) {
                player1.setMoney(player1.getMoney() - bet);
            } else {
                // Normal play
                while (!player1.playerBusted() && player1.getPlayerTotal() < 17) {
                    player1.hitPlayer();
                }
                while (!player1.dealerBusted() && player1.getDealerTotal() < 17) {
                    player1.hitDealer();
                }

                if (player1.playerBusted()) {
                    player1.setMoney(player1.getMoney() - bet);
                } else if (player1.dealerBusted()) {
                    player1.setMoney(player1.getMoney() + bet);
                } else if (player1.getPlayerTotal() > player1.getDealerTotal()) {
                    player1.setMoney(player1.getMoney() + bet);
                } else if (player1.getPlayerTotal() == player1.getDealerTotal()) {
                    // push — no change
                } else {
                    player1.setMoney(player1.getMoney() - bet);
                }
            }

            player1money[i] = player1.getMoney();
        }

        // --- Card counter bots (select which to simulate) ---
        System.out.println("Available bots:");
        System.out.println("1: Bot1 (Balanced)");
        System.out.println("2: Bot2 (Aggressive)");
        System.out.println("3: Bot3 (Conservative)");
        System.out.println("4: Bot4 (Flat)");
        System.out.println("5: Bot5 (Risky)");
        System.out.println("6: Bot6 (Percent)");
        System.out.println("7: Bot7 (Risky Percent)");
        System.out.println("8: Bot8 (Conservative Percent)");
        System.out.println("9: Bot9 (Kelly Criterion)");
        System.out.println("10: Bot10 (Half Kelly)");
        System.out.println("11: Bot11 (Quarter Kelly)");
        System.out.println("Enter bot numbers to simulate (comma-separated), or 'all' for all bots:");
        scanner.nextLine(); // consume leftover newline
        String selectionLine = scanner.nextLine().trim();

        boolean[] simulateBot = new boolean[12]; // index 1..11 for bots
        boolean anySelected = false;
        if (selectionLine.equalsIgnoreCase("all") || selectionLine.isEmpty()) {
            for (int i = 1; i <= 11; i++) simulateBot[i] = true;
            anySelected = true;
        } else {
            String[] toks = selectionLine.split("[,\\s]+");
            for (String t : toks) {
                try {
                    int idx = Integer.parseInt(t);
                    if (idx >= 1 && idx <= 11) {
                        simulateBot[idx] = true;
                        anySelected = true;
                    }
                } catch (NumberFormatException e) {
                    // ignore invalid tokens
                }
            }
        }
        if (!anySelected) {
            System.out.println("No valid bots selected — defaulting to all.");
            for (int i = 1; i <= 11; i++) simulateBot[i] = true;
        }

        // Always add the Basic Bot series
        XYSeries s0 = chart.addSeries("Basic Bot", steps, player1money);
        s0.setMarker(SeriesMarkers.NONE);
        s0.setLineWidth(0.5f);

        // Helper to add a bot series by index
        for (int i = 1; i <= 11; i++) {
            if (!simulateBot[i]) continue;
            double[] data = null;
            String label = "";
            switch (i) {
                case 1:  { Bot1 b = new Bot1(); data = b.simulate(totalSteps); label = "Bot1 (Balanced)"; break; }
                case 2:  { Bot2 b = new Bot2(); data = b.simulate(totalSteps); label = "Bot2 (Aggressive)"; break; }
                case 3:  { Bot3 b = new Bot3(); data = b.simulate(totalSteps); label = "Bot3 (Conservative)"; break; }
                case 4:  { Bot4 b = new Bot4(); data = b.simulate(totalSteps); label = "Bot4 (Flat)"; break; }
                case 5:  { Bot5 b = new Bot5(); data = b.simulate(totalSteps); label = "Bot5 (Risky)"; break; }
                case 6:  { percentBot b = new percentBot(); data = b.simulate(totalSteps); label = "Bot6 (Percent)"; break; }
                case 7:  { bot7 b = new bot7(); data = b.simulate(totalSteps); label = "Bot7 (Risky Percent)"; break; }
                case 8:  { bot8 b = new bot8(); data = b.simulate(totalSteps); label = "Bot8 (Conservative Percent)"; break; }
                case 9:  { kellybot b = new kellybot(); data = b.simulate(totalSteps); label = "Bot9 (Full Kelly)"; break; }
                case 10: { bot10 b = new bot10(); data = b.simulate(totalSteps); label = "Bot10 (Half Kelly)"; break; }
                case 11: { bot11 b = new bot11(); data = b.simulate(totalSteps); label = "Bot11 (Quarter Kelly)"; break; }
            }
            if (data != null) {
                XYSeries s = chart.addSeries(label, steps, data);
                s.setMarker(SeriesMarkers.NONE);
                s.setLineWidth(0.5f);
            }
        }

        try {
            BitmapEncoder.saveBitmap(chart, "blackjack_chart", BitmapFormat.PNG);
            System.out.println("Chart saved to blackjack_chart.png");
        } catch (IOException e) {
            System.out.println("Failed to save chart: " + e.getMessage());
        }

        scanner.close();
    }

    public static boolean isBlackjack(List<card> hand) {
        if (hand.size() != 2) return false;
        int v0 = hand.get(0).getValue();
        int v1 = hand.get(1).getValue();
        return (v0 == 1 && v1 == 10) || (v0 == 10 && v1 == 1);
    }

    public static int calcHandTotal(List<card> hand) {
        int total    = 0;
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

    public static boolean isSoftHand(List<card> hand) {
        int total    = 0;
        int aceCount = 0;
        for (card c : hand) {
            total += c.getValue();
            if (c.getValue() == 1) aceCount++;
        }
        return aceCount > 0 && (total + 10) <= 21;
    }

    public static boolean isPairHand(List<card> hand) {
        return hand.size() == 2
                && hand.get(0).getRank() == hand.get(1).getRank();
    }
}

// use: javac -cp "lib/xchart-3.8.8.jar;." -d bin (Get-ChildItem src\*.java)
// use: java -cp "lib/xchart-3.8.8.jar;bin" app

// chromebook: cd /workspaces/JavaProds/blackjackwalks && javac -d bin -cp "lib/*" src/*.java
//             cd /workspaces/JavaProds/blackjackwalks && java -cp "bin:lib/*" app