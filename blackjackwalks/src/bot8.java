import java.util.List;
import java.util.ArrayList;

public class bot8 {

    // Slightly more conservative betting than Bot1
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.CONSERVATIVE);
        return results;
    }
}
