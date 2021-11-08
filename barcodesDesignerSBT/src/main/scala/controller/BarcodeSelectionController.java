package main.code.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import main.code.ui.controls.IntegerField;
import main.code.ui.model.BarcodeSelectionModel;

import static main.code.ui.helpers.OtherHelpers.createTooltip;

/**
 * The controller class for the barcode selection page
 *
 * @author Marietta Hamberger
 */
public class BarcodeSelectionController {

    @FXML
    public Label numSolutionValueLabel;
    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    private GridPane contentPane;

    /**
     * A slider specifying how many individuals should be used in the GA
     */
    @FXML
    private Slider numSolutionsSlider;

    /**
     * An integer field specifying the number of generations for the GA
     */
    @FXML
    private IntegerField numIterationsField;

    /**
     * An integer field specifying the number of restarts for the GA
     */
    @FXML
    private IntegerField numRunsField;

    /**
     * A checkbox specifying whether the base positions in the barcode sets
     * should be approximately equally distributed between G/C and A/T
     * nucleotides
     */
    @FXML
    private CheckBox colorBalanceCheckbox;

    /**
     * An integer field specifying the number of parallel streams
     */
    @FXML
    private IntegerField numStreamsField;

    BarcodeSelectionModel view;

    /**
     * Initializes the controller
     * @param barcodeSelectionModel  The corresponding model class
     */
    public void init(BarcodeSelectionModel barcodeSelectionModel) {
        this.view = barcodeSelectionModel;

        // integer field specifying the number of iterations for the GA
        numIterationsField = new IntegerField(1, 1000000, 1000);
        numIterationsField.setPrefColumnCount(6);
        contentPane.add(numIterationsField, 1, 1);

        // integer field specifying the number of restarts of the GA
        numRunsField = new IntegerField(1, 100, 3);
        numRunsField.setPrefColumnCount(6);
        contentPane.add(numRunsField, 1, 2);

        // integer field specifying the number of streams
        numStreamsField = new IntegerField(1, 10, 4);
        numStreamsField.setPrefColumnCount(6);
        contentPane.add(numStreamsField, 1, 4);

        // add listener to update population size label
        numSolutionsSlider.valueProperty().addListener((observableValue, number, t1)
                -> numSolutionValueLabel.setText("(" + t1.intValue() + ")"));

        // initialize all tooltips
        initTooltips();
    }

    /**
     * Initializes all tooltips
     */
    private void initTooltips() {
        numIterationsField
                .setTooltip(createTooltip("Specifies the number of iterations of the optimization procedure " +
                                "(higher = higher quality, longer computation time)."));

        numRunsField
                .setTooltip(createTooltip("Specifies the number of independent runs of the optimization procedure " +
                                "(higher = higher quality, longer computation time)."));

        colorBalanceCheckbox
                .setTooltip(createTooltip("Option for Illumina sequencers: Specifies that the colors " +
                                "(red for A/C, green for G/T) should be balanced at each position of the barcodes " +
                                "(i.e. the number of A/C versus G/T nucleotides at a position should be approximately " +
                                "equal in the sets). More balanced sets will usually be smaller than less balanced sets."));

        numSolutionsSlider
                .setTooltip(createTooltip("Specifies how many barcode sets are evaluated to find the best ones " +
                                "(more = higher quality, longer computation time)."));

        numStreamsField
                .setTooltip(createTooltip("Specifies the number of parallel streams during the GA (" +
                        "recombination, mutation, fitness update)."));

    }

    /* GETTER & SETTER */

    /**
     * Gets the specified number of individuals for the GA
     * @return The number of individuals
     */
    public int getNumSolutions() {
        return numSolutionsSlider.valueProperty().intValue();
    }

    /**
     * Gets the number of iterations for the GA
     * @return The number of iterations
     */
    public int getNumIterations() {
        return numIterationsField.getValue();
    }

    /**
     * Gets the number of runs for the GA
     * @return The number of runs
     */
    public int getNumRuns() {
        return numRunsField.getValue();
    }

    /**
     * Determines whether the algorithm should balance
     * colors at the nucleotide positions
     * @return true if colors should be balanced, false otherwise
     */
    public boolean balanceColors() {
        return colorBalanceCheckbox.isSelected();
    }

    /**
     * Get the number of parallel streams for parts of the GA
     * @return The number of parallel streams
     */
    public int getNumStreams() { return numStreamsField.getValue(); }

}
