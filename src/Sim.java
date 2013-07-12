/**
 * Class Sim performs simulations of routing in the Pastry peer-to-peer
 * overlay by creating Pastry objects and randomly selecting source
 * and destination node IDs to calculate the hops for.  
 *
 * @authors  Richard Ballard, Sandesh Pardeshi, Mohanish Sawant
 */
import java.awt.Color;
import java.math.BigInteger;
import edu.rit.numeric.ListSeries;
import edu.rit.numeric.ListXYSeries;
import edu.rit.numeric.ListXYZSeries;
import edu.rit.numeric.Series;
import edu.rit.numeric.XYZSeries;
import edu.rit.numeric.plot.Plot;
import edu.rit.numeric.plot.Strokes;
import edu.rit.util.Random;

public class Sim {
    public static int nodeCount;
    public static int leafRange = 16;
    public static BigInteger[] nodes;
    public static int lowerBound;
    public static int trials;
    public static int seed;
    public static int offset;

    public static void main(String[] args) {
        // initialize all values if input format is correct
        if(args.length != 5) usage();
        lowerBound = Integer.parseInt(args[0]);
        nodeCount = Integer.parseInt(args[1]);
        seed = Integer.parseInt(args[2]);
        trials = Integer.parseInt(args[3]);
        offset = Integer.parseInt(args[4]);
       
        Random prng = Random.getInstance(seed);
        ListXYSeries mqpl = new ListXYSeries();
        ListXYZSeries model = new ListXYZSeries();
        ListXYSeries NHseries = new ListXYSeries();
        ListXYSeries NHseries1 = new ListXYSeries();
      

    // display table headers
    System.out.printf ("\tAverage Hops H%n");
    System.out.println("_____________________________________" + "\n");
    System.out.printf ("Nodes\tMean\t\tStddev%n");
       
        for (int j = lowerBound; j <= nodeCount; j= j+offset) {
            Pastry pastryobj = new Pastry(j, seed);
            ListSeries Hseries = new ListSeries();
            for (int tt = 0; tt < trials; tt++) {
                ListSeries hopSeries = new ListSeries();
                float sum = 0;
                for (int i = 0; i < trials; i++) {
                    int hops = pastryobj
            .route(prng.nextInt(j), prng.nextInt(j));
                    sum = sum + hops;
                    hopSeries.add(hops);
                }// for (int i = 0; i < trials; i++) {
                Hseries.add(hopSeries.stats().meanX);
            }// for (int tt = 0; tt < trials; tt++) {
            Series.Stats stats = Hseries.stats();
            double H = stats.meanX;
            double Hdev = stats.stddevX;
            model.add(Math.log10(j), H, Hdev);
            System.out.printf("%d\t%f\t%f%n", j, H, Hdev);

            mqpl.add(j, stats.meanX);
            NHseries.add(j, H);
            NHseries1.add(j,Math.log10(j));
       

        }// for (int j = lowerBound; j <= nodeCount; j += offset) {

        // Regression Class to calculate a and b model parameters.
        XYZSeries.Regression regr = model.linearRegression();
        System.out.printf("Q = a + b log N%n");
        System.out.printf("a = %.2f%n", regr.a);
        System.out.printf("b = %.2f%n", regr.b);
        System.out.printf("stddev(a) = %.2f%n", Math.sqrt(regr.var_a));
        System.out.printf("stddev(b) = %.2f%n", Math.sqrt(regr.var_b));
        System.out.printf("chi^2 = %.6f%n", regr.chi2);
        System.out.printf("p-value = %.6f%n", regr.significance);

        // Logarithmic plot of Nodes v/s the mean number of hops
        new Plot()
        .plotTitle("Pastry Plot (Log) : Trials = " + trials)
        .xAxisTitle("Number of nodes, N")
        .yAxisTitle("Mean no of hops, Q")
        .xAxisKind(Plot.LOGARITHMIC)
        .xAxisMinorDivisions(10)
        .xAxisMajorDivisions(4)
        .yAxisStart(1)
        .yAxisEnd(10)
        .yAxisMajorDivisions(9)
        .minorGridLines(true)
        .seriesStroke(null)
        .xySeries(mqpl)
        .seriesDots(null)
        .seriesStroke(Strokes.solid(1))
        .seriesColor(Color.red)
        .xySeries(regr.a + regr.b * lowerBound, Math.log10(lowerBound),
              regr.a + regr.b * nodeCount, Math.log10(nodeCount))
        .getFrame().setVisible(true);

        // Linear Plot of Nodes v/s the mean number of hops
        new Plot().plotTitle("Pastry Plot (Linear) : Trials = " + trials)
        .xAxisTitle("Number of nodes, N")
        .yAxisTitle("Mean number of hops").seriesStroke(null)
        .xySeries(mqpl).xAxisStart(lowerBound).xAxisEnd(nodeCount)
        .yAxisStart(1).yAxisEnd(10).xAxisMajorDivisions(4)
        .yAxisMajorDivisions(9).seriesColor(Color.red)
        .seriesDots(null).seriesStroke(Strokes.solid(1))
        .xySeries(NHseries1).getFrame().setVisible(true);
    }// main()

    /**
     * Returns the log base 16 of the passed in value.
     *
     * @param  double x
     */
    private static double log16(double x) {
        // Math.log is base e, natural log, ln
        return Math.log(x) / Math.log(16.0);
    }// log16(double x) {
   
    /**
     * Displays the correct input format for this program and exits.
     */
    public static void usage() {
        System.out.println("Usage: java Sim <lowerBound> <upperBound> <seed> <no of trials>");
        System.exit(1);
    }// usage()
}// Sim