import java.util.List;
import java.util.ArrayList;

public class Bot4 {

    // Flat betting bot
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.FLAT);
        return results;
    }
}
