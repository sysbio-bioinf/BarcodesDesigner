package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.code.controller.ResultController;
import main.code.ui.other.WizardPage;
import main.code.ui.helpers.ResourceLoader;

import java.io.IOException;


/**
 * A wizard page displaying barcode sets
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class ResultsModel extends WizardPage {

	private ResultController controller;
	/**
	 * Creates a new result wizard page
	 * @param prev	The previous wizard page
	 */
	public ResultsModel(WizardPage prev) throws IOException {
		this(prev, "Results of the barcode optimization procedure:");
	}
	
	/**
	 * Creates a new result wizard page with a custom description
	 * @param prev	The previous wizard page
	 * @param text	The description text for the label at the top
	 */
	public ResultsModel(WizardPage prev, String text) throws IOException {
		super(new GridPane(), text, prev);

		this.nextLabel.setValue("Close");
		this.disableCancel.setValue(true);

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("resultView");
		GridPane contentPane = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init();
		this.content = contentPane;
	}	

	@Override
	public boolean processNextRequest() {
		// this is usually the last page => close when clicking next
		((Stage)parent.getMainWizardPane().getScene().getWindow()).close();
		return false;
	}

	@Override
	public WizardPage getNext() {
		return null;
	}

	public ResultController getController() {
		return controller;
	}
}
