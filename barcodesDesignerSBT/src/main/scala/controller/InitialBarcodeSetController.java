package main.code.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.code.ui.model.GlobalFileDialogModel;
import main.code.ui.model.InitialBarcodeSetModel;

import java.io.IOException;

/**
 * The controller class for the generation/upload page of the initial barcode set
 *
 * @author Marietta Hamberger
 */
public class InitialBarcodeSetController {

    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    public GridPane contentPane;

    /**
     * The label associated with the choice "generate initial set"
     */
    @FXML
    private Label generateInitialSetLabel;

    /**
     * A radio button that specifies that the initial set should
     * be generated at random
     */
    @FXML
    private RadioButton generateInitialSetButton;

    /**
     * The label associated with the choice "load initial set"
     */
    @FXML
    private Label loadInitialSetLabel;

    /**
     * A radio button that specifies that the initial barcode set
     * should be loaded from a file
     */
    @FXML
    private RadioButton loadInitialSetButton;

    /**
     * A button to specify the file to load
     */
    @FXML
    private Button chooseFileButton;

    /**
     * A toggle group for the two options
     */
    private ToggleGroup barcodeSource;

    private InitialBarcodeSetModel view;

    /**
     * Initializes the controller
     * @param initialBarcodeSetModel  The corresponding model class
     */
    public void init(InitialBarcodeSetModel initialBarcodeSetModel){
        view = initialBarcodeSetModel;

        // set label texts
        loadInitialSetLabel.setText(new StringBuilder().append("Load initial barcode set from a file\n\n")
                .append("Loads the barcodes from a text file, where each row of the file corresponds to one barcode. ")
                .append("All barcodes must have the same length.").toString());
        generateInitialSetLabel.setText(new StringBuilder().append("Generate initial set randomly\n\n")
                .append("Generates an initial large set of barcodes with specific properties.").toString());

        // initialize bindings
        initBindings();
    }

    /**
     * Initialize all bindings
     */
    public void initBindings() {
        // create a toggle group to make the options mutually exclusive
        barcodeSource = new ToggleGroup();
        barcodeSource.getToggles().addAll(loadInitialSetButton, generateInitialSetButton);

        // add listener to enable/disable file chooser button depending on user choice
        barcodeSource.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == loadInitialSetButton) {
                        view.setDisableNext(view.getBarcodes() == null);
                        chooseFileButton.setDisable(false);
                    } else {
                        view.setDisableNext(false);
                        chooseFileButton.setDisable(true);
                    }
                });
    }

    /**
     * Opens file chooser for initial barcode set file
     * @throws IOException
     */
    public void openFileChooser() throws IOException {
        // load barcodes from a text file
        view.setBarcodes(GlobalFileDialogModel.chooseAndLoadBarcodes((Stage) contentPane
                .getScene().getWindow()));
        // automatically move to the next page
        if (view.getBarcodes() != null) {
            view.getParent().nextPage();
            view.setDisableNext(false);
        }
    }

    /* HELPER FUNCTIONS:
     * select respective button when label is clicked
     */
    public void selectLoadInitialSetButton() {
        loadInitialSetButton.setSelected(true);
    }

    public void selectGenerateInitialSetButton() {
        generateInitialSetButton.setSelected(true);
    }

    /* GETTER & SETTER */
    public ToggleGroup getBarcodeSource() {
        return barcodeSource;
    }

    public RadioButton getLoadInitialSetButton() {
        return loadInitialSetButton;
    }
}
