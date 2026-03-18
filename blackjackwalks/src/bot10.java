import java.util.List;
import java.util.ArrayList;

public class bot10 {
    // Half Kelly — same edges as full Kelly but bets 50% of the Kelly fraction
    // Lower growth rate than full Kelly but dramatically reduced variance/drawdowns
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        return base.simulateWithBetProfile(totalSteps, BetProfile.KELLY_HALF);
    }
}
