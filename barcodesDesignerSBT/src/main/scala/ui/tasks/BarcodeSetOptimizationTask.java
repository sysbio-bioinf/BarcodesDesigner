package main.code.ui.tasks;

import javafx.concurrent.Task;
import main.code.algorithm.BarcodeSetCollection;
import main.code.algorithm.BarcodeSetOptimizer;

/**
 * Task calling the barcode set optimization routines
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class BarcodeSetOptimizationTask extends Task<BarcodeSetCollection> {

	private int numBarcodes;
	private String barcodePattern;
	private double minGC;
	private double maxGC;
	private boolean generateHamming;
	private int numIndividuals;
	private int numOffspring; 
	private int numGenerations;
	private int earlyStoppingMinDist;
	private int numRuns;
	private int numStreams;

	/**
	 * Creates a new barcode optimization task 
	 * @param numBarcodes	The number of barcodes in the resulting sets
	 * @param barcodePattern	The pattern specifying which positions are set to 
	 * 							fixed nucleotides (A/C/G/T) or are set randomly (_)
	 * @param minGC				The minimum fraction of G/C nucleotides in each barcode
	 * @param maxGC				The maximum fraction of G/C nucleotides in each barcode
	 * @param generateHamming	Specifies whether Hamming codes should be generated or not
	 * @param numIndividuals	The number of individuals in the GA
	 * @param numOffspring	The number of offspring in the GA
	 * @param numRuns	The number of independent runs of the GA
	 * @param numStreams	The number of parallel streams
	 * @param numGenerations	The number of generations of the GA
	 * @param earlyStoppingMinDist	The minimum distance at which the algorithm terminates
	 * (or Integer.MAX_INT to disable early stopping)
	 */
	public BarcodeSetOptimizationTask(int numBarcodes,
                                      String barcodePattern,
                                      double minGC, double maxGC,
                                      boolean generateHamming,
                                      int numIndividuals, int numOffspring,
                                      int numRuns, int numGenerations, int earlyStoppingMinDist,
									  int numStreams)
	{
		this.numBarcodes = numBarcodes;
		this.barcodePattern = barcodePattern;
		this.minGC = minGC;
		this.maxGC = maxGC;
		this.generateHamming = generateHamming;
		this.numIndividuals = numIndividuals;
		this.numOffspring = numOffspring;
		this.numGenerations = numGenerations;
		this.numRuns = numRuns;
		this.earlyStoppingMinDist = earlyStoppingMinDist;
		this.numStreams = numStreams;
	}
	
	@Override
	public BarcodeSetCollection call() {
		updateProgress(0, 0);
		updateMessage("Barcode set optimization: Initializing...");
		// TimerHelper selectOptimalBarcodeTimer = new TimerHelper();
		// selectOptimalBarcodeTimer.start("Select Optimal Barcodes");
		BarcodeSetOptimizer b = new BarcodeSetOptimizer(barcodePattern, numBarcodes, minGC, maxGC, generateHamming);

		BarcodeSetCollection s = b.optimizeBarcodeSets(numIndividuals, numOffspring, numRuns,
				numGenerations, earlyStoppingMinDist, numStreams,
				(percentage, message) -> {
					updateProgress(percentage, 100);
					updateMessage("Barcode set optimization: " + message);
					return !isCancelled();
				});
		updateMessage("Barcode set optimization completed!");
		this.succeeded();
		// selectOptimalBarcodeTimer.end();
		// return the sets
		return s;
	}

}
