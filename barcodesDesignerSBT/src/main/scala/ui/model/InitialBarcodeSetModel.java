package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import main.code.controller.InitialBarcodeSetController;
import main.code.ui.other.WizardPage;
import main.code.ui.helpers.ResourceLoader;
import main.code.ui.other.BarcodeSource;

import java.io.IOException;

/**
 * A wizard page for choosing the initial barcode set
 * for barcode subset selection
 * @author Christoph Muessel
 *
 */
public class InitialBarcodeSetModel extends WizardPage {


	/**
	 * If barcodes are loaded from a file,
	 * this holds the loaded barcodes
	 */
	private String[] barcodes = null;
	
	/**
	 * The next page if barcodes are generated randomly
	 */
	private BarcodeGenerationModel generationPage = null;
	/**
	 * The next page if barcodes are loaded from a file
	 */
	private BarcodeSelectionModel selectionPage = null;


	InitialBarcodeSetController controller;

	/**
	 * Creates a new wizard page for the specification of the initial
	 * barcode set
	 * @param prev	The previous wizard page
	 */
	public InitialBarcodeSetModel(WizardPage prev) throws IOException {
		super(
				new GridPane(),
				"Specify the initial set of barcodes from which subsets should be chosen:",
				prev);

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("initialBarcodeSetView");
		GridPane contentPane = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
		this.content = contentPane;
	}

	/**
	 * Gets the chosen option for the initial barcode set
	 * @return The chosen option
	 */
	public BarcodeSource getBarcodeSource() {
		if (controller.getBarcodeSource().getSelectedToggle() == controller.getLoadInitialSetButton())
			return BarcodeSource.LOAD_FILE;

		return BarcodeSource.RANDOM_GENERATION;
	}

	/**
	 * Gets the existing barcodes if barcodes were loaded from a file 
	 * @return The barcodes
	 */
	public String[] getBarcodes() {
		return barcodes;
	}

	@Override
	public WizardPage getNext() throws IOException {
		// ensure existence of barcode selection parameters page which is either
		// the next page or the page after the next page depending on the choice
		if (selectionPage == null)
			selectionPage = new BarcodeSelectionModel(this);
		
		if (getBarcodeSource() == BarcodeSource.RANDOM_GENERATION)
		// next page is the barcode generation parameters page
		{
			if (generationPage == null)
				generationPage = new BarcodeGenerationModel(this, selectionPage, "Set the parameters for the generation of the initial barcode set:");
			// after the generation parameters, the selection parameters page should be shown
			selectionPage.setPrev(generationPage);
			return generationPage;
		}
		else
		// barcodes were loaded from a file => show the selection parameters page directly after this page
		{
			selectionPage.setPrev(this);
			return selectionPage;
		}
		
	}

	/* GETTER & SETTER */
	public void setBarcodes(String[] chooseAndLoadBarcodes) {
		barcodes = chooseAndLoadBarcodes;
	}

	public void setDisableNext(boolean b) {
		disableNext.setValue(b);
	}
}
