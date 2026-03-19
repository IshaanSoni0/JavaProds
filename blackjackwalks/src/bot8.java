import java.util.List;
import java.util.ArrayList;

public class bot8 {

    // Conservative percentage-based betting
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.CONSERVATIVE_PERCENT);
        return results;
    }
}
