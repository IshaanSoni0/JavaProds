import java.util.List;
import java.util.ArrayList;

public class bot11 {
    // Quarter Kelly — bets 25% of the Kelly fraction
    // Much smoother growth curve, minimal drawdowns, still positive EV
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        return base.simulateWithBetProfile(totalSteps, BetProfile.KELLY_QUARTER);
    }
}
