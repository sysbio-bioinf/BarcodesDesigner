package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import main.code.algorithm.BarcodeSetCollection;
import main.code.controller.ModeChoiceController;
import main.code.ui.other.WizardPage;
import main.code.ui.helpers.ResourceLoader;

import java.io.IOException;

/**
 * The first wizard page that allows for choosing between
 * different algorithms or loading previous results
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class ModeChoiceModel extends WizardPage {

	/**
	 * The next page if "barcode selection" has been chosen
	 */
	private InitialBarcodeSetModel initialBarcodeSetPage = null;
	/**
	 * The next page if "load results" has been chosen
	 */
	private ResultsModel loadedResultsPage = null;
	/**
	 * The next page if "barcode set generation" has been chosen
	 */
	private BarcodeGenerationModel generationPage = null;
	/**
	 * The page after the next page if "barcode set generation" has been chosen
	 */
	private BarcodeSetOptimizationModel setOptimizationPage = null;
	/**
	 * Contains the results loaded from a file if "load results" has been chosen
	 */
	private BarcodeSetCollection loadedResults = null;

	private ModeChoiceController controller;
	
	/**
	 * Creates a new initial choice wizard page
	 */
	public ModeChoiceModel() throws IOException {
		super(new GridPane(), "Please choose one of the following actions:", null);

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("modeChoiceView");
		GridPane contentPane = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
		this.content = contentPane;
	}

	@Override
	public WizardPage getNext() throws IOException {
		switch (controller.getSelection()) {
			case SELECT_SETS -> {
				// barcode selection chosen => move to the page for specifying the initial barcode set
				if (initialBarcodeSetPage == null)
					initialBarcodeSetPage = new InitialBarcodeSetModel(this);
				return initialBarcodeSetPage;
			}
			case LOAD_RESULTS -> {
				// previous results chosen => move to result's page
				if (loadedResultsPage == null)
					loadedResultsPage = new ResultsModel(this, "Barcode set(s) loaded from the file:");
				loadedResultsPage.getController().updateResults(loadedResults);
				return loadedResultsPage;
			}
			default -> {
				// barcode set generation chosen => move to the barcode generation parameters page
				// and create the optimization parameters page as its successor
				if (generationPage == null) {
					setOptimizationPage = new BarcodeSetOptimizationModel(null);
					generationPage = new BarcodeGenerationModel(this, setOptimizationPage, "Set the parameters for the generation of barcodes:");
					setOptimizationPage.setPrev(generationPage);
				}
				return generationPage;
			}
		}

	}

	public void setLoadedResults(BarcodeSetCollection chooseAndLoadBarcodeSets) {
		this.loadedResults = chooseAndLoadBarcodeSets;
	}

	public BarcodeSetCollection getLoadedResults() {
		return loadedResults;
	}

	public void setDisableNext(boolean b) {
		disableNext.setValue(b);
	}
}
