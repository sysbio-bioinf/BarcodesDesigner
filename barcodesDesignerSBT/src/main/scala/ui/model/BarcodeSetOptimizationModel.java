package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import main.code.algorithm.BarcodeSetCollection;
import main.code.controller.BarcodeSetOptimizationController;
import main.code.ui.helpers.ResourceLoader;
import main.code.ui.other.WizardPage;
import main.code.ui.tasks.BarcodeSetOptimizationTask;

import java.io.IOException;

/**
 * A page comprising the parameters for the 
 * barcode set optimization algorithm
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class BarcodeSetOptimizationModel extends WizardPage {

	BarcodeSetOptimizationController controller;

	/**
	 * Creates a new wizard page for setting the barcode set optimization parameters
	 * @param prev	The previous wizard page
	 */
	public BarcodeSetOptimizationModel(WizardPage prev) throws IOException {
		super(new GridPane(),
				"Set the parameters for the barcode set optimization procedure:", prev);

		// next step is the optimization itself => change button label
		nextLabel.setValue("Start");

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("barcodeSetOptimizationView");
		GridPane contentPane = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
		this.content = contentPane;

	}

	@Override
	public WizardPage getNext() throws IOException {
		// the next step is the optimization => create background task
		// and attach it to a progress-window
		BarcodeSetOptimizationTask task;
		BarcodeGenerationModel genPage = (BarcodeGenerationModel) prevPage;

		// create the results page to display the optimization results
		ResultsModel resultPage = new ResultsModel(this);
		
		// create the task with the parameters specified in the previous page
		task = new BarcodeSetOptimizationTask(genPage.getNumRandomBarcodes(),
					genPage.getBarcodePattern(), genPage.getMinGC(),
					genPage.getMaxGC(), genPage.useHammingCodes(),
					controller.getNumSolutions(), controller.getNumSolutions() * 2,
				controller.getNumRuns(), controller.getNumIterations(), controller.getNumStreams(),
				controller.getEarlyStoppingMinDist());
		
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
