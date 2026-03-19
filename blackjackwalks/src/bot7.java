import java.util.List;
import java.util.ArrayList;

public class bot7 {

    // Risky percentage-based betting
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.RISKY_PERCENT);
        return results;
    }
}
