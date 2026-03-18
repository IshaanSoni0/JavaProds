import java.util.List;
import java.util.ArrayList;

public class kellybot {
    //uses kelly criterion for bet sizing, with a simple estimation of win probability based on the true count
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.CONSERVATIVE);
        return results;
    }
}
