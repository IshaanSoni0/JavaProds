public class MeasureEdge {
    public static void main(String[] args) {
        System.out.println("Running 10,000,000 hands - BALANCED profile...");
        Bot1 bot1 = new Bot1();
        bot1.simulateWithBetProfile(10000000, BetProfile.BALANCED);

        System.out.println("Running 10,000,000 hands - FLAT profile (pure basic strategy, no counting)...");
        Bot1 bot2 = new Bot1();
        // Temporarily measure FLAT by abusing the BALANCED print trigger
        // We need to see the edge table for FLAT too
        // So we'll just print it manually by modifying the call
        // Actually FLAT won't print since it only prints for BALANCED
        // So let's just run BALANCED twice and note: if edge is same
        // regardless of rc, then basic strategy itself has -5% edge

        // Better: run a simple manual test
        // Deal 1,000,000 hands with flat $5 bet, basic strategy only, measure win rate
        playBlackjack game = new playBlackjack();
        game.setMoney(100000);
        game.resetRunningCount();

        int wins = 0, losses = 0, pushes = 0, hands = 0;
        double totalReturn = 0;

        for (int i = 0; i < 10000000; i++) {
            game.resetgame();
            if (game.deck.getlength() < 10) { game.resetDeck(); game.resetRunningCount(); }

            game.dealPlayer(); game.updateRunningCount(game.getPlayerHand().get(game.getPlayerHand().size()-1));
            game.dealDealer(); game.updateRunningCount(game.getDealerHand().get(game.getDealerHand().size()-1));
            game.dealPlayer(); game.updateRunningCount(game.getPlayerHand().get(game.getPlayerHand().size()-1));
            game.dealDealer();
            game.updateRunningCount(game.getDealerHand().get(1));

            // Handle blackjacks
            boolean pBJ = app.isBlackjack(game.getPlayerHand());
            boolean dBJ = app.isBlackjack(game.getDealerHand());
            if (pBJ || dBJ) {
                if      (pBJ && dBJ) { pushes++; }
                else if (pBJ)        { wins++;   totalReturn += 1.5; }
                else                 { losses++; totalReturn -= 1.0; }
                hands++;
                continue;
            }

            java.util.List<card> playerHand = new java.util.ArrayList<>(game.getPlayerHand());
            card dealerUp = game.getDealerCard(0);

            game.setBet(5); // set before playHand so doubles work (bet*2)
            int playerTotal = Bot1.playHand(playerHand, dealerUp, game);
            int finalBet    = game.getBet(); // 5 normally, 10 if doubled
            // dealer hit logic
            while (game.getDealerTotal() < 17) {
                game.hitDealer();
                game.updateRunningCount(game.getDealerHand().get(game.getDealerHand().size()-1));
            }
            int dealerTotal = game.getDealerTotal();

            int result = Bot1.resolveHand(playerTotal, dealerTotal, finalBet);
            if      (result > 0) wins++;
            else if (result < 0) losses++;
            else                 pushes++;
            totalReturn += (double) result / 5; // normalise by initial bet (5), not finalBet
            hands++;
        }

        System.out.println("\n=== Pure basic strategy measurement (BJ rounds included) ===");
        System.out.printf("Hands: %d%n", hands);
        System.out.printf("Wins:   %d (%.2f%%)%n", wins,   100.0*wins/hands);
        System.out.printf("Losses: %d (%.2f%%)%n", losses, 100.0*losses/hands);
        System.out.printf("Pushes: %d (%.2f%%)%n", pushes, 100.0*pushes/hands);
        System.out.printf("Average return per hand: %.4f (%.3f%%)%n", totalReturn/hands, totalReturn/hands*100);
        System.out.println();
        System.out.println("Expected: wins~43%, losses~49%, pushes~8%, edge~-0.5%");
        System.out.println("If edge is much worse than -0.5%, there is a strategy bug.");
        System.out.println("Done.");
    }
}

// chromebook: cd /workspaces/JavaProds/blackjackwalks && javac -cp lib/xchart-3.8.8.jar -d bin src/*.java 2>&1
//             cd /workspaces/JavaProds/blackjackwalks && java -cp bin:lib/xchart-3.8.8.jar MeasureEdge
// windows:    javac -cp "lib/xchart-3.8.8.jar;." -d bin src\*.java
//             java -cp "bin;lib/xchart-3.8.8.jar" MeasureEdge