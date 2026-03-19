import java.util.List;
import java.util.ArrayList;

public class bot12 {
    // Three-Quarter Kelly — bets 75% of the Kelly fraction
    // Retains ~94% of Full Kelly's growth rate at roughly half the variance
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        return base.simulateWithBetProfile(totalSteps, BetProfile.KELLY_THREE_QUARTER);
    }
}
