package main.code.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import main.code.ui.controls.IntegerField;
import main.code.ui.model.BarcodeSetOptimizationModel;

import static main.code.ui.helpers.OtherHelpers.createTooltip;

/**
 * The controller class for the barcode set optimization page
 *
 * @author Marietta Hamberger
 */
public class BarcodeSetOptimizationController {

    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    public GridPane contentPane;

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
     * A checkbox specifying that the algorithm should terminate early
     * if a certain minimum distance has been achieved
     */
    @FXML
    private CheckBox earlyStoppingCheckBox;

    /**
     * An integer field specifying the minimum distance for early stopping
     */
    @FXML
    private IntegerField earlyStoppingMinDistField;

    /**
     * An integer field specifying the number of restarts for the GA
     */
    @FXML
    private IntegerField numRunsField;

    /**
     * An integer field specifying the number of parallel streams
     */
    @FXML
    private IntegerField numStreamsField;

    private BarcodeSetOptimizationModel view;

    /**
     * Initializes the controller
     * @param barcodeSetOptimizationModel  The corresponding model class
     */
    public void init(BarcodeSetOptimizationModel barcodeSetOptimizationModel) {
        this.view = barcodeSetOptimizationModel;

        // integer field specifying the number of iterations for the GA
        numIterationsField = new IntegerField(1, 1000000, 1000);
        numIterationsField.setPrefColumnCount(6);
        contentPane.add(numIterationsField, 1, 1);

        // integer field specifying at which minimum distance the computation should terminate
        earlyStoppingMinDistField = new IntegerField(1, 100, 5);
        earlyStoppingMinDistField.setDisable(true);
        contentPane.add(earlyStoppingMinDistField, 1, 2);

        // integer field specifying the number of restarts of the GA
        numRunsField = new IntegerField(1, 100, 3);
        numRunsField.setPrefColumnCount(6);
        contentPane.add(numRunsField, 1, 3);

        // integer field specifying the number of streams
        numStreamsField = new IntegerField(1, 10, 4);
        numStreamsField.setPrefColumnCount(6);
        contentPane.add(numStreamsField, 1, 4);

        // initialize tooltips and bindings
        initTooltips();
        initBindings();
    }

    /**
     * Initializes all bindings
     */
    private void initBindings() {
        // enable/disable integer field for minimum distance based on corresponding check box
        earlyStoppingCheckBox.selectedProperty().addListener((ov, oldValue, newValue)
                -> earlyStoppingMinDistField.setDisable(!newValue));
    }

    /**
     * Initialize tooltips
     */
    private void initTooltips() {
        numSolutionsSlider
                .setTooltip(createTooltip("Specifies how many barcode sets are evaluated to find the best ones " +
                                "(more = higher quality, longer computation time)."));

        numIterationsField
                .setTooltip(createTooltip("Specifies the number of iterations of the optimization procedure " +
                                "(higher = higher quality, longer computation time)."));

        numRunsField
                .setTooltip(createTooltip("Specifies the number of independent runs of the optimization procedure " +
                                "(higher = higher quality, longer computation time)."));

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
     * Get the number of parallel streams for parts of the GA
     * @return The number of parallel streams
     */
    public int getNumStreams() { return numStreamsField.getValue(); }

    /**
     * Gets the minimum distance for early stopping
     * (or Integer.MAX_VALUE if the criterion is disabled)
     * @return The minimum distance
     */
    public int getEarlyStoppingMinDist() {
        if (earlyStoppingCheckBox.isSelected())
            return earlyStoppingMinDistField.getValue();
        else
            return Integer.MAX_VALUE;
    }
}
