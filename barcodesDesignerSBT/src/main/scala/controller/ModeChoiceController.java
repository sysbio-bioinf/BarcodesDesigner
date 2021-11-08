package main.code.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import main.code.ui.model.GlobalFileDialogModel;
import main.code.ui.model.ModeChoiceModel;
import main.code.ui.other.WizardMode;


import java.io.IOException;

import static main.code.ui.other.WizardMode.SELECT_SETS;

public class ModeChoiceController {

    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    public GridPane contentPane;

    /**
     * A box packing the "barcode set generation" button and its label
     */
    @FXML
    public HBox barcodeSetGenerationBox;

    /**
     * The radio button associated with the choice "barcode set generation"
     */
    @FXML
    public RadioButton barcodeSetGenerationButton;

    /**
     * The label associated with the choice "barcode set generation"
     */
    @FXML
    public Label barcodeSetGenerationLabel;

    /**
     * A box packing the "barcode selection" button and its label
     */
    @FXML
    public HBox barcodeSelectionBox;

    /**
     * The radio button associated with the choice "barcode selection"
     */
    @FXML
    public RadioButton barcodeSelectionButton;

    /**
     * The label associated with the choice "barcode selection"
     */
    @FXML
    public Label barcodeSelectionLabel;

    /**
     * A box packing the "load results" button and its label
     */
    @FXML
    public HBox loadResultsBox;

    /**
     * The radio button associated with the choice "load results"
     */
    @FXML
    public RadioButton loadResultsButton;

    /**
     * The label associated with the choice "load results"
     */
    @FXML
    public Label loadResultsLabel;

    /**
     * A button that opens a file chooser when results should be loaded
     */
    @FXML
    public Button chooseFileButton;

    /**
     * A toggle group for the three choices
     */
    private ToggleGroup modeChoice;

    private ModeChoiceModel view;

    /**
     * Initializes the controller
     * @param modeChoiceModel  The corresponding model class
     */
    public void init(ModeChoiceModel modeChoiceModel){
        view = modeChoiceModel;

        // set label texts
        barcodeSetGenerationLabel.setText(new StringBuilder().append("Optimize a fixed-size barcode set\n\n")
                .append("Generates a new set of barcodes with a predefined size and high pairwise distances between " +
                        "any two barcodes.").toString());
        barcodeSelectionLabel.setText(new StringBuilder().append("Select optimal subsets from an existing barcode set\n\n")
                .append("Chooses subsets of barcodes with high pairwise distances from an existing initial set of barcodes.")
                .toString());
        loadResultsLabel.setText(new StringBuilder().append("View existing barcode set(s)\n\n")
                .append("Loads barcode sets from text files, or displays the results of previous Barcode " +
                        "Designer optimization runs.").toString());

        // initialize bindings
        initBindings();
    }

    /**
     * Initialize all bindings
     */
    public void initBindings() {
        // create a toggle group to make the options mutually exclusive
        modeChoice = new ToggleGroup();
        modeChoice.getToggles().addAll(barcodeSetGenerationButton, barcodeSelectionButton, loadResultsButton);

        // add listener to enable/disable file chooser button depending on user choice
        modeChoice.selectedToggleProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // "Next" button is disabled if no results have been loaded
                    if (newValue == loadResultsButton) {
                        view.setDisableNext(view.getLoadedResults() == null);
                        chooseFileButton.setDisable(false);
                    } else {
                        view.setDisableNext(false);
                        chooseFileButton.setDisable(true);
                    }
                });
    }

    public void openFileChooser() {
        // load the barcode set from the specified file
        view.setLoadedResults(GlobalFileDialogModel. chooseAndLoadBarcodeSets((Stage) contentPane
                .getScene().getWindow()));

        // automatically move to next page when results have been loaded
        if (view.getLoadedResults() != null) {
            try {
                view.getParent().nextPage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            view.setDisableNext(false);
        }
    }

    /* HELPER FUNCTIONS:
    * select respective button when label is clicked
    */
    public void selectBarcodeSetGenerationButton() {
        barcodeSetGenerationButton.setSelected(true);
    }

    public void selectBarcodeSelectionButton() {
        barcodeSelectionButton.setSelected(true);
    }

    public void selectLoadResultsButton() {
        loadResultsButton.setSelected(true);
    }

    /* GETTER & SETTER */

    /**
     * Gets the choice made on the wizard page
     * @return	The selected item
     */
    public WizardMode getSelection()
    {
        if (modeChoice.getSelectedToggle() == barcodeSelectionButton)
            return SELECT_SETS;

        if (modeChoice.getSelectedToggle() == loadResultsButton)
            return WizardMode.LOAD_RESULTS;

        return WizardMode.GENERATE_SETS;
    }
}
