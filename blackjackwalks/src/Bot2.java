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
    static final int PERCENT = 6;
    static final int RISKY_PERCENT = 7;
    static final int CONSERVATIVE_PERCENT = 8;
    static final int KELLY                = 9;
    static final int KELLY_HALF           = 10;
    static final int KELLY_QUARTER        = 11;
    static final int KELLY_THREE_QUARTER  = 12;
}
