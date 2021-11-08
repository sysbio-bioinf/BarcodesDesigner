package main.code.ui.tasks;

import javafx.concurrent.Task;
import main.code.algorithm.BarcodeGenerator;
import main.code.algorithm.BarcodeSelector;
import main.code.algorithm.BarcodeSetCollection;
import main.code.algorithm.InitType;

/**
 * Task calling the barcode selection routines
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class BarcodeSelectionTask extends Task<BarcodeSetCollection> {

	/**
	 * The complete set of candidate barcodes
	 */
	private String[] barcodes; 
	
	/**
	 * Gets the full set of candidate barcodes
	 * @return The candidate barcodes
	 */
	public String[] getBarcodes() {
		return barcodes;
	}

	private int numRandomBarcodes;
	private String barcodePattern;
	private double minGC;
	private double maxGC;
	private boolean generateHamming;
	private int numIndividuals;
	private int numOffspring; 
	private int numGenerations;
	private int numRuns;
	private boolean balanceColors;
	private InitType initialization;
	private boolean generateBarcodes;
	private int numStreams;
	
	/**
	 * Creates a new barcode selection task from pre-specified candidate barcodes
	 * @param barcodes	The barcodes from which a subset should be selected
	 * @param numIndividuals	The number of individuals in the GA
	 * @param numOffspring	The number of offspring in the GA
	 * @param numRuns	The number of independent runs of the GA
	 * @param numGenerations	The number of generations of the GA
	 * @param numStreams		The number of parallel streams for recombination, mutation, evaluation
	 * @param balanceColors		Specifies that colors (A/C versus G/T nucleotides) should
	 * 							be balanced for each position across the barcode set
	 * @param initialization	A parameter specifying how the GA should be initialized
	 * (see barcodedesigner.algorithm.InitType)
	 */
	public BarcodeSelectionTask(String[] barcodes, 
								int numIndividuals, int numOffspring, 
								int numRuns, int numGenerations, boolean balanceColors,
								int numStreams, InitType initialization)
	{
		this.barcodes = barcodes;
		this.numIndividuals = numIndividuals;
		this.numOffspring = numOffspring;
		this.numGenerations = numGenerations;
		this.numRuns = numRuns;
		this.balanceColors = balanceColors;
		this.initialization = initialization;
		this.numStreams = numStreams;
		this.generateBarcodes = false;
	}
	
	/**
	 * Creates a new barcode selection task from randomly generated candidate barcodes
	 * @param numRandomBarcodes	The number of random barcodes to be generated
	 * @param barcodePattern	The pattern specifying which positions are set to 
	 * 							fixed nucleotides (A/C/G/T) or are set randomly (_)
	 * @param minGC				The minimum fraction of G/C nucleotides in each barcode
	 * @param maxGC				The maximum fraction of G/C nucleotides in each barcode
	 * @param generateHamming	Specifies whether Hamming codes should be generated or not
	 * @param numIndividuals	The number of individuals in the GA
	 * @param numOffspring	The number of offspring in the GA
	 * @param numRuns	The number of independent runs of the GA
	 * @param balanceColors		Specifies that colors (A/C versus G/T nucleotides) should
	 * 							be balanced for each position across the barcode set
	 * @param numGenerations	The number of generations of the GA
	 * @param numStreams		The number of parallel streams for recombination, mutation, evaluation
	 * @param initialization	A parameter specifying how the GA should be initialized
	 * (see barcodedesigner.algorithm.InitType)
	 */
	public BarcodeSelectionTask(int numRandomBarcodes,
			String barcodePattern,
			double minGC, double maxGC,
			boolean generateHamming,
			int numIndividuals, int numOffspring, 
			int numRuns, int numGenerations, boolean balanceColors,
								int numStreams, InitType initialization)
	{
		this.numRandomBarcodes = numRandomBarcodes;
		this.barcodePattern = barcodePattern;
		this.minGC = minGC;
		this.maxGC = maxGC;
		this.generateHamming = generateHamming;
		this.numIndividuals = numIndividuals;
		this.numOffspring = numOffspring;
		this.numGenerations = numGenerations;
		this.numRuns = numRuns;
		this.balanceColors = balanceColors;
		this.initialization = initialization;
		this.numStreams = numStreams;
		this.generateBarcodes = true;
	}
	
	@Override
	public BarcodeSetCollection call() {
		if (generateBarcodes){
			// first generate the barcodes at random
			updateProgress(0, 0);
			// TimerHelper generateBarcodesTimer = new TimerHelper();
			// generateBarcodesTimer.start("Generate Barcodes");
			BarcodeGenerator g = new BarcodeGenerator(barcodePattern, minGC, maxGC,
					(percentage, message) -> {
						updateProgress(percentage, 100);
						updateMessage("Barcode generation: " + message);
						return !isCancelled();
					});
			barcodes = g.generateBarcodes(numRandomBarcodes, 1000, generateHamming);
			// generateBarcodesTimer.end();
		}
		// now select maximum-distance subsets of barcodes
		updateProgress(0, 0);
		updateMessage("Barcode selection: Initializing...");
		BarcodeSelector b = new BarcodeSelector(barcodes);

		System.out.println("We have " + barcodes.length + " barcodes");
		// TimerHelper selectOptimalBarcodeTimer = new TimerHelper();
		// selectOptimalBarcodeTimer.start("Select Optimal Barcodes");
		BarcodeSetCollection s = b.selectBarcodesGA(numIndividuals, numOffspring, numRuns, numGenerations,
				balanceColors, numStreams, initialization, (percentage, message) -> {
					updateProgress(percentage, 100);
					updateMessage("Barcode selection: " + message);
					return !isCancelled();
				});
		updateMessage("Barcode selection completed!");
		this.succeeded();
		// selectOptimalBarcodeTimer.end();
		// return the sets
		return s;
	}

}
