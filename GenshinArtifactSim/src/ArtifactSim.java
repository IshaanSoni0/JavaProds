import java.util.*;
import java.io.*;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.statistics.*;
import org.jfree.data.xy.*;

/**
 * Genshin Impact 5-Star Artifact Simulator
 *
 * Data sources (from provided game data):
 *   - Main stat probabilities: per slot from wiki images
 *   - Sub-stat weights: HP/ATK/DEF=6, HP%/ATK%/DEF%/ER%/EM=4, CR%/CD%=3
 *   - 5-star sub-stat max values: standard game values
 *   - Roll tiers (5-star): 100%/90%/80%/70% of max, each 25%
 *   - Upgrade schedule: +4, +8, +12, +16, +20  -> 5 total upgrades
 *   - 4-sub start chance: 20% (standard 5-star game value)
 *
 * Outputs:
 *   1. Main stat counts per slot type
 *   2 & 3. Per-piece final sub-stat rolls at +20, with starter type
 *   5. Average sub-stat value per slot type
 *   6. Crit averages filtered to pieces with >=1 crit sub
 *
 *  Compile : javac -d bin src/ArtifactSim.java
 *  Run     : java -cp bin ArtifactSim
 */
public class ArtifactSim {

    // ── Slot & sub-stat names ─────────────────────────────────────────────────

    static final String[] SLOT_NAMES = {
        "Flower of Life", "Plume of Death", "Sands of Eon",
        "Goblet of Eonothem", "Circlet of Logos"
    };

    /** Sub-stat pool indices (0-9) */
    static final String[] SUB_NAMES = {
        "HP",                  // 0
        "ATK",                 // 1
        "DEF",                 // 2
        "HP%",                 // 3
        "ATK%",                // 4
        "DEF%",                // 5
        "Energy Recharge%",    // 6
        "Elemental Mastery",   // 7
        "CRIT Rate%",          // 8
        "CRIT DMG%"            // 9
    };
    static final int NUM_SUBS   = 10;
    static final int IDX_CRIT_R = 8;
    static final int IDX_CRIT_D = 9;

    // ── Sub-stat mechanics (from images) ──────────────────────────────────────

    /** Relative weights used when drawing sub-stats */
    static final int[] SUB_WEIGHTS = { 6, 6, 6, 4, 4, 4, 4, 4, 3, 3 };

    /**
     * Maximum roll value for each sub-stat on a 5-star artifact.
     * Roll tiers: 100 / 90 / 80 / 70 % of max, each at 25% chance.
     */
    static final double[] SUB_MAX = {
        298.75,  // HP
         19.45,  // ATK
         23.15,  // DEF
          5.83,  // HP%
          5.83,  // ATK%
          7.29,  // DEF%
          6.48,  // Energy Recharge%
         23.31,  // Elemental Mastery
          3.89,  // CRIT Rate%
          7.77   // CRIT DMG%
    };

    static final double[] ROLL_TIERS = { 1.0, 0.9, 0.8, 0.7 }; // 25% each (5-star)

    /** Chance to start with 4 sub-stats instead of 3 (5-star standard) */
    static final double FOUR_SUB_CHANCE = 0.20;

    static final Random RNG = new Random();

    // ── Artifact data class ───────────────────────────────────────────────────

    static final class Artifact {
        final int     slot;
        final String  mainStat;
        final int     mainExclude; // sub pool index blocked by main stat (-1 = not in pool)
        final boolean fourStart;
        final int[]    subIdx  = new int[4];   // sub-stat indices (-1 = unfilled)
        final double[] subVal  = new double[4]; // accumulated roll values
        final int[]    subRolls = new int[4];   // total roll events per sub (initial + upgrades)

        Artifact(int slot, String mainStat, int mainExclude, boolean fourStart) {
            this.slot        = slot;
            this.mainStat    = mainStat;
            this.mainExclude = mainExclude;
            this.fourStart   = fourStart;
            Arrays.fill(subIdx, -1);
        }
    }

    // ── Roll helpers ──────────────────────────────────────────────────────────

    /** Single roll value for a given sub-stat (random tier, 5-star). */
    static double rollTier(int subIndex) {
        return SUB_MAX[subIndex] * ROLL_TIERS[RNG.nextInt(4)];
    }

    /**
     * Draw one sub-stat from the weighted pool, excluding already-used indices.
     * @param excluded boolean array; true = this sub index is unavailable
     */
    static int pickSub(boolean[] excluded) {
        int total = 0;
        for (int i = 0; i < NUM_SUBS; i++) if (!excluded[i]) total += SUB_WEIGHTS[i];
        int r = RNG.nextInt(total);
        for (int i = 0; i < NUM_SUBS; i++) {
            if (excluded[i]) continue;
            r -= SUB_WEIGHTS[i];
            if (r < 0) return i;
        }
        return -1; // unreachable
    }

    // ── Main stat rollers (per slot) ──────────────────────────────────────────
    // Returns Object[]{ "stat name", subPoolExcludeIndex }
    // subPoolExcludeIndex = -1 when the main stat is not in the sub-stat pool
    // (e.g. elemental DMG bonuses, Healing Bonus%)

    static Object[] rollMainStat(int slot) {
        switch (slot) {
            case 0: return new Object[]{ "HP (flat)",  0 };  // Flower: always HP flat
            case 1: return new Object[]{ "ATK (flat)", 1 };  // Plume:  always ATK flat
            case 2: return rollSandsMain();
            case 3: return rollGobletMain();
            case 4: return rollCircletMain();
        }
        throw new IllegalStateException("Unknown slot: " + slot);
    }

    // Sands: HP% 26.68 | ATK% 26.66 | DEF% 26.66 | ER% 10.00 | EM 10.00
    static Object[] rollSandsMain() {
        double r = RNG.nextDouble() * 100.0;
        if (r <  26.68) return new Object[]{ "HP%",              3 };
        if (r <  53.34) return new Object[]{ "ATK%",             4 };
        if (r <  80.00) return new Object[]{ "DEF%",             5 };
        if (r <  90.00) return new Object[]{ "Energy Recharge%", 6 };
                        return new Object[]{ "Elemental Mastery", 7 };
    }

    // Goblet: HP% 19.25 | ATK% 19.25 | DEF% 19.00 | 7xEle DMG 5.00 | Phys 5.00 | EM 2.50
    static Object[] rollGobletMain() {
        double r = RNG.nextDouble() * 100.0;
        if (r <  19.25) return new Object[]{ "HP%",                  3  };
        if (r <  38.50) return new Object[]{ "ATK%",                 4  };
        if (r <  57.50) return new Object[]{ "DEF%",                 5  };
        if (r <  62.50) return new Object[]{ "Pyro DMG Bonus%",     -1  };
        if (r <  67.50) return new Object[]{ "Electro DMG Bonus%",  -1  };
        if (r <  72.50) return new Object[]{ "Cryo DMG Bonus%",     -1  };
        if (r <  77.50) return new Object[]{ "Hydro DMG Bonus%",    -1  };
        if (r <  82.50) return new Object[]{ "Dendro DMG Bonus%",   -1  };
        if (r <  87.50) return new Object[]{ "Anemo DMG Bonus%",    -1  };
        if (r <  92.50) return new Object[]{ "Geo DMG Bonus%",      -1  };
        if (r <  97.50) return new Object[]{ "Physical DMG Bonus%", -1  };
                        return new Object[]{ "Elemental Mastery",    7  };
    }

    // Circlet: HP% 22 | ATK% 22 | DEF% 22 | CR% 10 | CD% 10 | Healing 10 | EM 4
    static Object[] rollCircletMain() {
        double r = RNG.nextDouble() * 100.0;
        if (r <  22.00) return new Object[]{ "HP%",             3  };
        if (r <  44.00) return new Object[]{ "ATK%",            4  };
        if (r <  66.00) return new Object[]{ "DEF%",            5  };
        if (r <  76.00) return new Object[]{ "CRIT Rate%",      8  };
        if (r <  86.00) return new Object[]{ "CRIT DMG%",       9  };
        if (r <  96.00) return new Object[]{ "Healing Bonus%",  -1 };
                        return new Object[]{ "Elemental Mastery", 7 };
    }

    // ── Core simulation ───────────────────────────────────────────────────────

    /**
     * Simulate a single 5-star artifact leveled to +20.
     *
     * Upgrade schedule (5-star, +20):
     *   - 5 upgrade events total at +4, +8, +12, +16, +20
     *   - 3-sub start: event 1 unlocks the 4th sub; events 2-5 upgrade existing subs
     *   - 4-sub start: all 5 events upgrade existing subs
     *   Each upgrade event selects one of the 4 sub slots uniformly at random.
     */
    static Artifact simulate(int slot) {
        Object[] mainRoll  = rollMainStat(slot);
        String   mainName  = (String) mainRoll[0];
        int      mainExcl  = (int)    mainRoll[1];
        boolean  fourStart = RNG.nextDouble() < FOUR_SUB_CHANCE;

        Artifact art = new Artifact(slot, mainName, mainExcl, fourStart);

        // Track which sub-stat pool slots are taken
        boolean[] excluded = new boolean[NUM_SUBS];
        if (mainExcl >= 0) excluded[mainExcl] = true;

        // Roll initial sub-stats
        int initCount = fourStart ? 4 : 3;
        for (int i = 0; i < initCount; i++) {
            int idx = pickSub(excluded);
            art.subIdx[i]   = idx;
            art.subVal[i]   = rollTier(idx);
            art.subRolls[i] = 1;
            excluded[idx]   = true;
        }

        // 5 upgrade events
        int upgradesLeft = 5;
        if (!fourStart) {
            // First event: unlock the 4th sub-stat slot
            int idx = pickSub(excluded);
            art.subIdx[3]   = idx;
            art.subVal[3]   = rollTier(idx);
            art.subRolls[3] = 1;
            excluded[idx]   = true;
            upgradesLeft    = 4;
        }
        // Remaining events: upgrade a random existing sub slot
        for (int u = 0; u < upgradesLeft; u++) {
            int pick = RNG.nextInt(4);
            art.subVal[pick]   += rollTier(art.subIdx[pick]);
            art.subRolls[pick] += 1;
        }

        return art;
    }

    // ── Output helpers ────────────────────────────────────────────────────────

    // ── Chart generation ─────────────────────────────────────────────────────

    /**
     * Generates 2 PNG charts per slot type into a "charts_N<N>" sub-folder:
     *   [Slot]_histogram.png     — relative-frequency histogram of (2×CR%)+CD%
     *                             across ALL N pieces (zeros for non-crit pieces
     *                             form the spike on the left).
     *   [Slot]_distribution.png — normal distribution curve fitted to
     *                             crit-positive pieces only, with a mean marker.
     */
    @SuppressWarnings("unchecked")
    static void generateCharts(List<Artifact>[] data, int N, String outputDir) {
        try {
            File chartDir = new File(outputDir, "charts_N" + N);
            chartDir.mkdirs();
            System.out.println("Generating charts → " + chartDir.getAbsolutePath());

            for (int s = 0; s < 5; s++) {
                String slot     = SLOT_NAMES[s];
                String fileSafe = slot.replace(" ", "_");

                // ── collect per-piece (2×CR%)+CD% values ─────────────────
                double[] allVals = new double[N];
                for (int i = 0; i < N; i++) {
                    Artifact a = data[s].get(i);
                    double cv = 0;
                    for (int j = 0; j < 4; j++) {
                        if (a.subIdx[j] == IDX_CRIT_R) cv += 2.0 * a.subVal[j];
                        if (a.subIdx[j] == IDX_CRIT_D) cv +=       a.subVal[j];
                    }
                    allVals[i] = cv;
                }

                // stats on crit-positive subset for distribution fit
                double[] critVals = Arrays.stream(allVals).filter(v -> v > 0).toArray();
                double sumC = 0, sum2C = 0;
                for (double v : critVals) { sumC += v; sum2C += v * v; }
                double meanC   = sumC   / critVals.length;
                double stddevC = Math.sqrt(sum2C / critVals.length - meanC * meanC);

                // ── Chart 1: Histogram (all pieces) ──────────────────────
                HistogramDataset hds = new HistogramDataset();
                hds.setType(HistogramType.RELATIVE_FREQUENCY);
                hds.addSeries("All pieces", allVals, 80);

                JFreeChart histChart = ChartFactory.createHistogram(
                    slot + "  ·  (2×CR%) + CD%  —  All " + N + " Pieces",
                    "(2×CR%) + CD%  Crit Value", "Relative Frequency",
                    hds, PlotOrientation.VERTICAL, false, false, false);

                XYPlot hp = histChart.getXYPlot();
                hp.setBackgroundPaint(Color.WHITE);
                hp.setDomainGridlinePaint(Color.LIGHT_GRAY);
                hp.setRangeGridlinePaint(Color.LIGHT_GRAY);
                XYBarRenderer hr = (XYBarRenderer) hp.getRenderer();
                hr.setShadowVisible(false);
                hr.setSeriesPaint(0, new Color(65, 105, 225, 180));
                hr.setDrawBarOutline(false);

                ChartUtils.saveChartAsPNG(
                    new File(chartDir, fileSafe + "_histogram.png"),
                    histChart, 1000, 580);

                // ── Chart 2: KDE distribution (crit-positive pieces) ───
                // Bandwidth via Silverman's rule of thumb: h = 1.06 * σ * n^(-1/5)
                double lo = Math.max(0, meanC - 4.5 * stddevC);
                double hi =             meanC + 4.5 * stddevC;
                double h  = 1.06 * stddevC * Math.pow(critVals.length, -0.2);

                // Bin crit values into a fine grid for efficient O(BINS*GRID) KDE
                final int KDE_BINS = 4000, KDE_GRID = 600;
                long[] binCounts = new long[KDE_BINS];
                double kdeRange = hi - lo;
                for (double v : critVals) {
                    int b = (int)((v - lo) / kdeRange * KDE_BINS);
                    if (b >= 0 && b < KDE_BINS) binCounts[b]++;
                }

                XYSeries ns = new XYSeries(
                    String.format("KDE  (h=%.3f,  \u03bc=%.2f, \u03c3=%.2f,  n=%,d crit pieces)",
                        h, meanC, stddevC, critVals.length));
                double kdeDenom = critVals.length * h * Math.sqrt(2.0 * Math.PI);
                for (int k = 0; k <= KDE_GRID; k++) {
                    double x = lo + kdeRange * k / KDE_GRID;
                    double density = 0;
                    for (int b = 0; b < KDE_BINS; b++) {
                        if (binCounts[b] == 0) continue;
                        double bc = lo + (b + 0.5) * kdeRange / KDE_BINS;
                        density += binCounts[b] * Math.exp(-0.5 * Math.pow((x - bc) / h, 2));
                    }
                    ns.add(x, density / kdeDenom);
                }

                JFreeChart distChart = ChartFactory.createXYLineChart(
                    slot + "  ·  (2×CR%) + CD%  —  KDE Distribution (crit pieces only)",
                    "(2×CR%) + CD%  Crit Value", "Probability Density",
                    new XYSeriesCollection(ns),
                    PlotOrientation.VERTICAL, true, false, false);

                XYPlot dp = distChart.getXYPlot();
                dp.setBackgroundPaint(Color.WHITE);
                dp.setDomainGridlinePaint(Color.LIGHT_GRAY);
                dp.setRangeGridlinePaint(Color.LIGHT_GRAY);
                XYLineAndShapeRenderer dr = (XYLineAndShapeRenderer) dp.getRenderer();
                dr.setDefaultShapesVisible(false);
                dr.setSeriesPaint(0, new Color(200, 40, 40));
                dr.setSeriesStroke(0, new BasicStroke(2.5f));
                // dashed vertical line at mean
                dp.addDomainMarker(new ValueMarker(meanC,
                    new Color(40, 160, 40),
                    new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, new float[]{6f, 3f}, 0f)));

                ChartUtils.saveChartAsPNG(
                    new File(chartDir, fileSafe + "_distribution.png"),
                    distChart, 1000, 580);

                // ── Chart 3: Expected maximum CV vs. number of artifacts farmed ───
                // E[max of n iid draws] = integral_0^inf (1 - F(x)^n) dx
                // Computed via left Riemann sum over empirical CDF binned from simulation data.
                double[] sortedAV = Arrays.copyOf(allVals, N);
                Arrays.sort(sortedAV);
                final int CDF_BINS = 2000, MAX_FARM = 500;
                double avMax = sortedAV[N - 1];
                double bw = avMax / CDF_BINS;

                // Build empirical CDF at right edge of each bin via binary search
                double[] cdfEdge = new double[CDF_BINS];
                for (int b = 0; b < CDF_BINS; b++) {
                    double edge = bw * (b + 1);
                    int bLo = 0, bHi = N;
                    while (bLo < bHi) {
                        int m = (bLo + bHi) >>> 1;
                        if (sortedAV[m] <= edge) bLo = m + 1; else bHi = m;
                    }
                    cdfEdge[b] = (double) bLo / N;
                }

                XYSeries maxSeries = new XYSeries("Expected best CV across n artifacts farmed");
                for (int n = 1; n <= MAX_FARM; n++) {
                    double eMax = 0, prevF = 0.0;
                    for (int b = 0; b < CDF_BINS; b++) {
                        eMax += bw * (1.0 - Math.pow(prevF, n));
                        prevF = cdfEdge[b];
                    }
                    maxSeries.add(n, eMax);
                }

                JFreeChart maxChart = ChartFactory.createXYLineChart(
                    slot + "  ·  Expected Best CV vs. Artifacts Farmed",
                    "Number of Artifacts Farmed", "Expected Best (2×CR%) + CD%",
                    new XYSeriesCollection(maxSeries),
                    PlotOrientation.VERTICAL, true, false, false);

                XYPlot mp = maxChart.getXYPlot();
                mp.setBackgroundPaint(Color.WHITE);
                mp.setDomainGridlinePaint(Color.LIGHT_GRAY);
                mp.setRangeGridlinePaint(Color.LIGHT_GRAY);
                XYLineAndShapeRenderer mr = (XYLineAndShapeRenderer) mp.getRenderer();
                mr.setDefaultShapesVisible(false);
                mr.setSeriesPaint(0, new Color(130, 60, 200));
                mr.setSeriesStroke(0, new BasicStroke(2.5f));

                ChartUtils.saveChartAsPNG(
                    new File(chartDir, fileSafe + "_expected_max.png"),
                    maxChart, 1000, 580);

                // ── Chart 4: Inverse CDF / Quantile function ─────────────
                // x = percentile (0–100), y = CV at that percentile
                // Uses the already-sorted sortedAV array from chart 3.
                final int QUANTILE_POINTS = 1000;
                XYSeries qSeries = new XYSeries("CV by percentile (all pieces)");
                for (int k = 0; k <= QUANTILE_POINTS; k++) {
                    double pct = 100.0 * k / QUANTILE_POINTS;
                    int idx = (int) Math.min((long) k * N / QUANTILE_POINTS, N - 1);
                    qSeries.add(pct, sortedAV[idx]);
                }

                JFreeChart quantileChart = ChartFactory.createXYLineChart(
                    slot + "  ·  Quantile Function  (CV by Percentile)",
                    "Percentile", "(2×CR%) + CD%  Crit Value",
                    new XYSeriesCollection(qSeries),
                    PlotOrientation.VERTICAL, true, false, false);

                XYPlot qp = quantileChart.getXYPlot();
                qp.setBackgroundPaint(Color.WHITE);
                qp.setDomainGridlinePaint(Color.LIGHT_GRAY);
                qp.setRangeGridlinePaint(Color.LIGHT_GRAY);
                XYLineAndShapeRenderer qr = (XYLineAndShapeRenderer) qp.getRenderer();
                qr.setDefaultShapesVisible(false);
                qr.setSeriesPaint(0, new Color(210, 120, 0));
                qr.setSeriesStroke(0, new BasicStroke(2.5f));

                ChartUtils.saveChartAsPNG(
                    new File(chartDir, fileSafe + "_quantile.png"),
                    quantileChart, 1000, 580);

                System.out.printf("  [%s]  histogram + distribution + expected_max + quantile saved.%n", slot);
            }
        } catch (IOException e) {
            System.err.println("Chart generation failed: " + e.getMessage());
        }
    }

    static void header(String title) {
        System.out.println();
        System.out.println("=".repeat(72));
        System.out.println("  " + title);
        System.out.println("=".repeat(72));
    }

    /**
     * Prints the >= 1..5 crit-roll threshold table for a list of artifacts.
     * @param pieces   the filtered artifact list to analyse
     * @param totalN   the overall N for the slot (used for "% of all N" column)
     */
    static void printCritRollTable(List<Artifact> pieces, int totalN) {
        if (pieces.isEmpty()) {
            System.out.println("      (no qualifying pieces)");
            return;
        }
        // Pre-compute total crit rolls per piece
        int[] rollCounts = new int[pieces.size()];
        for (int i = 0; i < pieces.size(); i++) {
            Artifact a = pieces.get(i);
            int tot = 0;
            for (int j = 0; j < 4; j++)
                if (a.subIdx[j] == IDX_CRIT_R || a.subIdx[j] == IDX_CRIT_D)
                    tot += a.subRolls[j];
            rollCounts[i] = tot;
        }
        System.out.printf("      %-16s  %-26s  %-22s  %-22s  %-24s  %s%n",
            "Threshold",
            "Count of qualifying (%qual)",
            "% of all N pieces",
            "Avg CR% (sub)",
            "Avg CD% (sub)",
            "Avg (2*CR%)+CD%/piece");
        for (int thresh = 1; thresh <= 7; thresh++) {
            double crT = 0.0, cdT = 0.0, combT = 0.0;
            int crC = 0, cdC = 0, cnt = 0;
            for (int i = 0; i < pieces.size(); i++) {
                if (rollCounts[i] < thresh) continue;
                cnt++;
                Artifact a = pieces.get(i);
                double pCR = 0.0, pCD = 0.0;
                for (int j = 0; j < 4; j++) {
                    if (a.subIdx[j] == IDX_CRIT_R) { pCR += a.subVal[j]; crT += a.subVal[j]; crC++; }
                    if (a.subIdx[j] == IDX_CRIT_D) { pCD += a.subVal[j]; cdT += a.subVal[j]; cdC++; }
                }
                combT += (2.0 * pCR) + pCD;
            }
            String label = ">= " + thresh + " crit roll" + (thresh == 1 ? " " : "s");
            if (cnt == 0) {
                System.out.printf("      %-16s  %5d / %-8d (%6.2f%%)   (%5.2f%% of all N)   (no pieces)%n",
                    label, cnt, pieces.size(), 0.0, 0.0);
            } else {
                System.out.printf("      %-16s  %5d / %-8d (%6.2f%%)   (%5.2f%% of all N)   CR%% avg = %7.4f%%   CD%% avg = %7.4f%%   (2*CR%%)+CD%% = %7.4f%%%n",
                    label,
                    cnt, pieces.size(), 100.0 * cnt / pieces.size(),
                    100.0 * cnt / totalN,
                    crC > 0 ? crT / crC : 0.0,
                    cdC > 0 ? cdT / cdC : 0.0,
                    combT / cnt);
            }
        }
    }

    // ── Main ─────────────────────────────────────────────────────────────────

    // ── Tee stream: writes to both the original stdout and a file ──────────
    static class TeeOutputStream extends OutputStream {
        private final OutputStream a, b;
        TeeOutputStream(OutputStream a, OutputStream b) { this.a = a; this.b = b; }
        @Override public void write(int c)              throws IOException { a.write(c);    b.write(c);    }
        @Override public void write(byte[] buf, int off, int len) throws IOException { a.write(buf,off,len); b.write(buf,off,len); }
        @Override public void flush()                   throws IOException { a.flush();    b.flush();    }
    }

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of artifacts to simulate per slot type: ");
        int N = sc.nextInt();
        sc.close();

        if (N <= 0) {
            System.out.println("Error: N must be a positive integer.");
            return;
        }

        // Redirect System.out to tee: console + results file
        String outFileName = "artifact_sim_results_N" + N + ".txt";
        FileOutputStream fos = new FileOutputStream(outFileName);
        PrintStream tee = new PrintStream(new TeeOutputStream(System.out, fos), true);
        PrintStream originalOut = System.out;
        System.setOut(tee);
        System.out.println("Results will be saved to: " + new File(outFileName).getAbsolutePath());

        // ------------------------------------------------------------------
        // Run simulation
        // ------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        List<Artifact>[] data = new List[5];
        for (int s = 0; s < 5; s++) {
            data[s] = new ArrayList<>(N);
            for (int i = 0; i < N; i++) data[s].add(simulate(s));
        }
        System.out.printf("Simulated %,d artifacts x 5 slots = %,d total.%n", N, 5 * N);

        // ==================================================================
        // OUTPUT 1 — Main stat counts per slot type
        // ==================================================================
        header("1. MAIN STAT COUNTS PER SLOT TYPE");
        for (int s = 0; s < 5; s++) {
            System.out.println("\n  " + SLOT_NAMES[s] + ":");
            Map<String, Integer> counts = new TreeMap<>();
            for (Artifact a : data[s]) counts.merge(a.mainStat, 1, Integer::sum);
            counts.forEach((name, cnt) ->
                System.out.printf("    %-28s  %7d  (%5.2f%%)%n",
                    name, cnt, 100.0 * cnt / N));
        }

        // ==================================================================
        // OUTPUT 2 & 3 — Per-piece final rolls at +20 + starter type
        // ==================================================================
        header("2 & 3. PER-PIECE FINAL ROLLS AT +20  (with starter type)");

        final int PRINT_LIMIT = 100;
        if (N > PRINT_LIMIT)
            System.out.printf(
                "  Note: N=%d > %d — printing first %d pieces per slot.%n" +
                "  Aggregated statistics (sections 5 & 6) use all %,d pieces.%n",
                N, PRINT_LIMIT, PRINT_LIMIT, N);

        for (int s = 0; s < 5; s++) {
            System.out.println("\n  -- " + SLOT_NAMES[s] + " --");
            int shown = 0;
            for (Artifact a : data[s]) {
                if (shown >= PRINT_LIMIT) {
                    System.out.printf("  ... (%d more pieces not shown)%n", N - PRINT_LIMIT);
                    break;
                }
                System.out.printf("  #%-5d  Main: %-28s  [%s]%n",
                    ++shown,
                    a.mainStat,
                    a.fourStart ? "4-substat start" : "3-substat start");
                for (int j = 0; j < 4; j++) {
                    if (a.subIdx[j] >= 0)
                        System.out.printf("           %-24s  %.4f%n",
                            SUB_NAMES[a.subIdx[j]], a.subVal[j]);
                }
            }
        }

        // ==================================================================
        // OUTPUT 5 — Average sub-stat value per slot type
        // ==================================================================
        header("5. AVERAGE SUB-STAT VALUES PER SLOT TYPE  (all " + N + " pieces)");
        for (int s = 0; s < 5; s++) {
            System.out.println("\n  " + SLOT_NAMES[s] + ":");
            double[] totals = new double[NUM_SUBS];
            int[]    cnts   = new int[NUM_SUBS];
            for (Artifact a : data[s]) {
                for (int j = 0; j < 4; j++) {
                    if (a.subIdx[j] >= 0) {
                        totals[a.subIdx[j]] += a.subVal[j];
                        cnts[a.subIdx[j]]++;
                    }
                }
            }
            for (int i = 0; i < NUM_SUBS; i++) {
                if (cnts[i] > 0)
                    System.out.printf(
                        "    %-24s  avg(all N) = %9.4f   avg(holders only) = %9.4f   (%d / %d pieces, %5.1f%%)%n",
                        SUB_NAMES[i],
                        totals[i] / N,
                        totals[i] / cnts[i],
                        cnts[i], N,
                        100.0 * cnts[i] / N);
            }
            // Crit value = (2*CR%) + CD%, averaged across all N pieces (non-holders contribute 0)
            double crAllN = totals[IDX_CRIT_R] / N;
            double cdAllN = totals[IDX_CRIT_D] / N;
            System.out.printf("    %-24s  avg(all N) = %9.4f%n",
                "(2*CR%)+CD% crit value", (2.0 * crAllN) + cdAllN);
        }

        // ==================================================================
        // OUTPUT 6 — Crit averages, filtered to pieces with >=1 crit sub
        // ==================================================================
        header("6. CRIT SUB-STAT AVERAGES  (filter: pieces with >= 1 crit sub)");
        for (int s = 0; s < 5; s++) {
            double crTotal = 0.0, cdTotal = 0.0;
            int    crCount = 0,   cdCount = 0,   critPieces = 0;
            for (Artifact a : data[s]) {
                boolean hasCrit = false;
                for (int j = 0; j < 4; j++) {
                    if (a.subIdx[j] == IDX_CRIT_R) { crTotal += a.subVal[j]; crCount++; hasCrit = true; }
                    if (a.subIdx[j] == IDX_CRIT_D) { cdTotal += a.subVal[j]; cdCount++; hasCrit = true; }
                }
                if (hasCrit) critPieces++;
            }
            // Denominator is crCount/cdCount within the crit-filtered set.
            // Since non-crit pieces are excluded, this is always >= the section 5 all-N average.
            System.out.printf("%n  %-24s  %5d / %d pieces have >= 1 crit sub  (%.1f%%)%n",
                SLOT_NAMES[s], critPieces, N, 100.0 * critPieces / N);
            System.out.printf("    Avg CRIT Rate%%  = %7.4f%%   (%d of %d crit-filtered pieces had CR sub)%n",
                crCount > 0 ? crTotal / crCount : 0.0, crCount, critPieces);
            System.out.printf("    Avg CRIT DMG%%   = %7.4f%%   (%d of %d crit-filtered pieces had CD sub)%n",
                cdCount > 0 ? cdTotal / cdCount : 0.0, cdCount, critPieces);
            // Crit value per filtered piece: (2*CR%) + CD%, with non-holders contributing 0 within the set
            double cvPerPiece = critPieces > 0
                ? ((2.0 * crTotal) + cdTotal) / critPieces : 0.0;
            System.out.printf("    Avg (2*CR%%)+CD%% crit value (per crit-filtered piece) = %7.4f%%%n", cvPerPiece);
        }

        // ==================================================================
        // OUTPUT 7 — Double-crit sub analysis
        // For Flower/Plume/Sands/Goblet: pieces with BOTH CR% and CD% as subs.
        // For Circlet: pieces with a crit main stat AND at least one crit sub.
        // Reports: % of pieces qualifying, roll-count thresholds, crit averages.
        // ==================================================================
        header("7. DOUBLE-CRIT / CRIT-MAIN ANALYSIS");
        System.out.println("  (Flower/Plume/Sands/Goblet: both CR% and CD% as subs)");
        System.out.println("  (Circlet: crit main stat + >= 1 crit sub)");

        for (int s = 0; s < 5; s++) {
            List<Artifact> filtered = new ArrayList<>();
            for (Artifact a : data[s]) {
                boolean hasCR = false, hasCD = false;
                for (int j = 0; j < 4; j++) {
                    if (a.subIdx[j] == IDX_CRIT_R) hasCR = true;
                    if (a.subIdx[j] == IDX_CRIT_D) hasCD = true;
                }
                if (s == 4) { // Circlet
                    boolean critMain = a.mainExclude == IDX_CRIT_R || a.mainExclude == IDX_CRIT_D;
                    if (critMain && (hasCR || hasCD)) filtered.add(a);
                } else {
                    if (hasCR && hasCD) filtered.add(a);
                }
            }

            System.out.printf("%n  %s%n", SLOT_NAMES[s]);
            System.out.printf("    Qualifying pieces : %d / %d  (%.2f%%)%n",
                filtered.size(), N, 100.0 * filtered.size() / N);

            if (filtered.isEmpty()) continue;

            // Count total crit rolls per filtered piece
            int[] critRollCounts = new int[filtered.size()];
            for (int i = 0; i < filtered.size(); i++) {
                Artifact a = filtered.get(i);
                int total = 0;
                for (int j = 0; j < 4; j++)
                    if (a.subIdx[j] == IDX_CRIT_R || a.subIdx[j] == IDX_CRIT_D)
                        total += a.subRolls[j];
                critRollCounts[i] = total;
            }

            // For each threshold >= 1..5: show % of qualifying pieces that meet it,
            // then averages computed ONLY from pieces with >= that many crit rolls.
            // averages will rise with threshold since low-roll pieces are excluded.
            System.out.println("    Crit roll distribution (each row = only pieces with >= N crit rolls):");
            printCritRollTable(filtered, N);
        }

        // ==================================================================
        // OUTPUT 8 — Same as section 7 but broken down by main stat per slot
        // ==================================================================
        header("8. DOUBLE-CRIT / CRIT-MAIN ANALYSIS  (broken down by main stat)");
        System.out.println("  (Flower/Plume/Sands/Goblet: both CR% and CD% as subs)");
        System.out.println("  (Circlet: crit main stat + >= 1 crit sub)");

        for (int s = 0; s < 5; s++) {
            System.out.printf("%n  %s%n", SLOT_NAMES[s]);

            // Group qualifying pieces by main stat
            Map<String, List<Artifact>> byMain = new TreeMap<>();
            for (Artifact a : data[s]) {
                boolean hasCR = false, hasCD = false;
                for (int j = 0; j < 4; j++) {
                    if (a.subIdx[j] == IDX_CRIT_R) hasCR = true;
                    if (a.subIdx[j] == IDX_CRIT_D) hasCD = true;
                }
                boolean qualifies;
                if (s == 4) {
                    boolean critMain = a.mainExclude == IDX_CRIT_R || a.mainExclude == IDX_CRIT_D;
                    qualifies = critMain && (hasCR || hasCD);
                } else {
                    qualifies = hasCR && hasCD;
                }
                if (qualifies)
                    byMain.computeIfAbsent(a.mainStat, k -> new ArrayList<>()).add(a);
            }

            if (byMain.isEmpty()) {
                System.out.println("    (no qualifying pieces)");
                continue;
            }

            for (Map.Entry<String, List<Artifact>> entry : byMain.entrySet()) {
                String mainName = entry.getKey();
                List<Artifact> group = entry.getValue();
                System.out.printf("    Main stat: %-28s  %d / %d qualifying  (%.2f%% of all N)%n",
                    mainName, group.size(), N, 100.0 * group.size() / N);
                printCritRollTable(group, N);
            }
        }

        System.out.println();
        System.out.println("=".repeat(72));
        System.out.println("  Simulation complete.");
        System.out.println("=".repeat(72));

        // Flush and close the file, restore original stdout for final message
        tee.flush();
        fos.close();
        System.setOut(originalOut);
        System.out.println("Saved to: " + new File(outFileName).getAbsolutePath());

        // Generate charts (runs after file is closed so output goes to console only)
        generateCharts(data, N, ".");
    }
}
//javac -d bin -cp "lib/*" src/ArtifactSim.java
//java  -cp "bin;lib/*" ArtifactSim
