import java.util.List;
import java.util.ArrayList;

public class Bot5 {

    // Riskier betting than Bot1
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.RISKY);
        return results;
    }
}
