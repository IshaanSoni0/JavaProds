import java.util.List;
import java.util.ArrayList;

public class Bot2 {

    // Slightly more aggressive betting than Bot1
    public double[] simulate(int totalSteps) {
        Bot1 base = new Bot1();
        double[] results = base.simulateWithBetProfile(totalSteps, BetProfile.AGGRESSIVE);
        return results;
    }
}

class BetProfile {
    static final int AGGRESSIVE = 1;
    static final int BALANCED = 2;
    static final int CONSERVATIVE = 3;
    static final int FLAT = 4;
    static final int RISKY = 5;
}
