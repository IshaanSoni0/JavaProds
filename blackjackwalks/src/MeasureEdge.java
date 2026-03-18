public class MeasureEdge {
    public static void main(String[] args) {
        System.out.println("Running 1,000,000 hands to measure edge per count level...");
        Bot1 bot = new Bot1();
        bot.simulateWithBetProfile(1000000, BetProfile.BALANCED);
        System.out.println("Done.");
    }
}

// chromebook: cd /workspaces/JavaProds/blackjackwalks && javac -cp lib/xchart-3.8.8.jar -d bin src/*.java 2>&1
//             cd /workspaces/JavaProds/blackjackwalks && java -cp bin:lib/xchart-3.8.8.jar MeasureEdge
// windows:    javac -cp "lib/xchart-3.8.8.jar;." -d bin src\*.java
//             java -cp "bin;lib/xchart-3.8.8.jar" MeasureEdge