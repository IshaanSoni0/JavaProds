import org.knowm.xchart.*;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
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

        // --- PLAYER 2: Card counter bots (Bot1..Bot5) ---
        Bot1 bot = new Bot1();
        Bot2 bot2 = new Bot2();
        Bot3 bot3 = new Bot3();
        Bot4 bot4 = new Bot4();
        Bot5 bot5 = new Bot5();

        double[] b1 = bot.simulate(totalSteps);
        double[] b2 = bot2.simulate(totalSteps);
        double[] b3 = bot3.simulate(totalSteps);
        double[] b4 = bot4.simulate(totalSteps);
        double[] b5 = bot5.simulate(totalSteps);

        chart.addSeries("Basic Bot", steps, player1money);
        chart.addSeries("Bot1", steps, b1);
        chart.addSeries("Bot2", steps, b2);
        chart.addSeries("Bot3", steps, b3);
        chart.addSeries("Bot4", steps, b4);
        chart.addSeries("Bot5", steps, b5);
        try {
            BitmapEncoder.saveBitmap(chart, "blackjack_chart", BitmapFormat.PNG);
            System.out.println("Chart saved to blackjack_chart.png");
        } catch (IOException e) {
            System.out.println("Failed to save chart: " + e.getMessage());
        }

        scanner.close();
    }

    // Helper to calculate hand total with soft Ace logic
    public static int calcHandTotal(List<card> hand) {
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
    public static boolean isSoftHand(List<card> hand) {
        int total = 0;
        int aceCount = 0;
        for (card c : hand) {
            total += c.getValue();
            if (c.getValue() == 1) aceCount++;
        }
        return aceCount > 0 && (total + 10) <= 21;
    }

    // Helper to detect pair (two cards same rank)
    public static boolean isPairHand(List<card> hand) {
        return hand.size() == 2 && hand.get(0).getRank() == hand.get(1).getRank();
    }
}


// use: javac -cp "lib/xchart-3.8.8.jar;." -d bin (Get-ChildItem src\*.java)    // to compile with XChart
// use: java -cp "lib/xchart-3.8.8.jar;bin" app    // to run the app
