package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import main.code.algorithm.BarcodeSetCollection;
import main.code.algorithm.InitType;
import main.code.controller.BarcodeSelectionController;
import main.code.ui.helpers.ResourceLoader;
import main.code.ui.other.WizardPage;
import main.code.ui.tasks.BarcodeSelectionTask;
import java.io.IOException;

/**
 * A wizard page holding the parameters for barcode subset selection
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class BarcodeSelectionModel extends WizardPage {

	private BarcodeSelectionController controller;

	/**
	 * Creates a new wizard page for setting the barcode subset selection parameters
	 * @param prev	The previous wizard page
	 */
	public BarcodeSelectionModel(WizardPage prev) throws IOException {
		super(new GridPane(),
				"Set the parameters for the selection of barcode subsets:", prev);

		// next step is the optimization itself => change button label
		nextLabel.setValue("Start");

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("barcodeSelectionView");
		this.content = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
	}

	@Override
	public WizardPage getNext() throws IOException {

		// the next step is the optimization => create background task
		// and attach it to a progress-window
		BarcodeSelectionTask task;
		if (prevPage instanceof BarcodeGenerationModel genPage) {
			// initial barcodes are generated randomly => obtain parameters from the previous page
			task = new BarcodeSelectionTask(genPage.getNumRandomBarcodes(),
					genPage.getBarcodePattern(), genPage.getMinGC(),
					genPage.getMaxGC(), genPage.useHammingCodes(),
					controller.getNumSolutions(), controller.getNumSolutions() * 2,
					controller.getNumRuns(), controller.getNumIterations(),
					controller.balanceColors(), controller.getNumStreams(),
					InitType.INIT_FORWARD());
		} else {
			// initial barcodes are loaded from a file => obtain the loaded barcodes from the previous page
			InitialBarcodeSetModel bcPage = (InitialBarcodeSetModel) prevPage;
			task = new BarcodeSelectionTask(bcPage.getBarcodes(),
					controller.getNumSolutions(), controller.getNumSolutions() * 2,
					controller.getNumRuns(), controller.getNumIterations(),
					controller.balanceColors(), controller.getNumStreams(),
					InitType.INIT_FORWARD());
		}

		// create the results page to display the optimization results
		ResultsModel resultPage = new ResultsModel(this);

		// create the progress page for the task
		ProgressModel<BarcodeSetCollection> progressPage = new ProgressModel<>("Barcode selection progress",
				this, resultPage, task);

		// attach a listener to the result of the task to show the results page
		// as soon as the optimization has terminated
		progressPage.returnValue.addListener((observable, oldValue, newValue) -> {
			// update the results
			resultPage.getController().updateResults(newValue);
			// automatically move to the results page
			parent.gotoPage(resultPage);
		});

		// next page is the progress page
		return progressPage;
	}



}
