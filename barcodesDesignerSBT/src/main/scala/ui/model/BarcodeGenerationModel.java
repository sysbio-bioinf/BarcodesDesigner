package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import main.code.controller.BarcodeGenerationController;
import main.code.ui.other.WizardPage;
import main.code.ui.helpers.ResourceLoader;
import java.io.IOException;

/**
 * A wizard page specifying the parameters for the generation of random barcode sets
 * (used in different contexts by both algorithms)
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class BarcodeGenerationModel extends WizardPage {
	
	/**
	 * The next wizard page (depending on the context)
	 */
	private WizardPage nextPage;

	private BarcodeGenerationController controller;

	/**
	 * Creates a new wizard page for setting the barcode set generation parameters
	 * @param prev	The previous wizard page
	 * @param next	The next wizard page
	 * @param text	The text for the caption label at the top of the wizard
	 */
	public BarcodeGenerationModel(WizardPage prev, WizardPage next, String text) throws IOException {

		super(new GridPane(), text, prev);
		this.nextPage = next;

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("barcodeGenerationView");
		GridPane contentPane = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
		this.content = contentPane;
	}
	
	@Override
	public WizardPage getNext() {
		return nextPage;
	}

	/**
	 * Returns the specified number of barcodes to be generated
	 * @return	The number of barcodes
	 */
	public int getNumRandomBarcodes() {
		return controller.getNumRandomBarcodes();
	}
	
	/**
	 * Returns the barcode pattern string specifying the fixed/non-fixed nucleotides
	 * @return	The barcode pattern
	 */
	public String getBarcodePattern() {
		return controller.getBarcodePattern();
	}
	
	/**
	 * Gets the minimum fraction of G/C nucleotides in each barcode
	 * @return	The minimum fraction of G/C nucleotides
	 */
	public double getMinGC() {
		return controller.getMinGC() / 100.0;
	}
	
	/**
	 * Gets the maximum fraction of G/C nucleotides in each barcode
	 * @return	The maximum fraction of G/C nucleotides
	 */
	public double getMaxGC() {
		return controller.getMaxGC() / 100.0;
	}
	
	/**
	 * Determines whether Hamming codes should be generated
	 * @return	true if Hamming codes should be generated, false otherwise
	 */
	public boolean useHammingCodes() {
		return controller.getHammingCodeCheckBox().isSelected();
	}

}
