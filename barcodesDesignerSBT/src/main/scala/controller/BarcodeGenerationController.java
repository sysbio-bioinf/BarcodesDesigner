package main.code.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import main.code.algorithm.BarcodeDistanceCalculator;
import main.code.ui.controls.BarcodePatternField;
import main.code.ui.controls.IntegerField;
import main.code.ui.model.BarcodeGenerationModel;

import static main.code.ui.helpers.OtherHelpers.createTooltip;

/**
 * The controller class for the barcode generation page
 *
 * @author Marietta Hamberger
 */
public class BarcodeGenerationController {

    /**
     * A choice box specifying the used distance metric
     */
    @FXML
    public ChoiceBox distanceMetricChoiceBox;

    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    private GridPane contentPane;

    /**
     * A label holding the string "%"
     */
    @FXML
    public Label percentageLabel;

    /**
     * An integer field specifying the length of randomly generated barcodes
     */
    @FXML
    private IntegerField barcodeLengthField;

    /**
     * An integer field specifying the number of randomly generated barcodes
     */
    @FXML
    private IntegerField numRandomBarcodesField;

    /**
     * A text field holding a pattern that specifies which positions of the
     * barcodes are fixed to specific nucleotides and which are set at random
     */
    @FXML
    private BarcodePatternField barcodePatternField;

    /**
     * An inner panel containing the two fields for minimum and maximum G/C
     * percentage
     */
    @FXML
    private GridPane gcRangeGrid;

    /**
     * An integer field specifying the minimum percentage of G/C nucleotides in
     * each barcode
     */
    @FXML
    private IntegerField minGCField;

    /**
     * An integer field specifying the maximum percentage of G/C nucleotides in
     * each barcode
     */
    @FXML
    private IntegerField maxGCField;

    /**
     * A checkbox specifying whether Hamming codes should be generated or not
     */
    @FXML
    private CheckBox hammingCodeCheckBox;

    /**
     * The corresponding model for the barcode generation page
     */
    BarcodeGenerationModel view;

    /**
     * Initializes the controller
     * @param barcodeGenerationModel  The corresponding model class
     */
    public void init(BarcodeGenerationModel barcodeGenerationModel) {
        view = barcodeGenerationModel;
        percentageLabel.setText("%");
        initIntegerFields();
        initPatternField();

        initBindings();
    }

    /**
     * Initializes the barcode pattern field
     */
    private void initPatternField() {

        // create a checkbox to specify whether Hamming codes should be generated
        hammingCodeCheckBox = new CheckBox("Initialize with Hamming codes");
        hammingCodeCheckBox.setWrapText(true);
        contentPane.add(hammingCodeCheckBox, 0, 4, 2, 1);
        hammingCodeCheckBox
                .setTooltip(createTooltip("Uses code words from a Hamming code, " +
                        "which ensures that the initial set already has a minimum distance of 3 nucleotides. " +
                        "However, this reduces the total number of barcodes as compared to purely random generation."));
        hammingCodeCheckBox.setSelected(true);
    }

    /**
     * Initializes all IntegerFields
     */
    private void initIntegerFields() {

        // input for barcode length
        barcodeLengthField = new IntegerField(1, 100, 12);
        barcodeLengthField
                .setTooltip(createTooltip("Enter the number of nucleotides in a barcode here."));
        barcodeLengthField.setPrefColumnCount(6);
        contentPane.add(barcodeLengthField, 1, 0);

        // input for number of randomly generated barcodes
        numRandomBarcodesField = new IntegerField(2, 1000000, 100);
        numRandomBarcodesField.setPrefColumnCount(6);
        numRandomBarcodesField
                .setTooltip(createTooltip("Enter the number of randomly generated candidate barcodes here. " +
                        "These barcodes serve as the input for the barcode set selection (higher = longer computation time, " +
                        "larger sets)."));
        contentPane.add(numRandomBarcodesField, 1, 1);

        // input for barcode pattern
        barcodePatternField = new BarcodePatternField(
                barcodeLengthField.getValue());
        barcodePatternField
                .setTooltip(createTooltip("Enter fixed nucleotides (A/C/G/T) here. Positions marked with _ " +
                        "are assigned randomly."));
        contentPane.add(barcodePatternField, 1, 2);

        // input for gc content range
        minGCField = new IntegerField(0, 100, 40);
        maxGCField = new IntegerField(0, 100, 60);

        gcRangeGrid.add(minGCField, 0, 0);
        gcRangeGrid.add(maxGCField, 2, 0);
        gcRangeGrid.setAlignment(Pos.BASELINE_RIGHT);
        final Tooltip gcTooltip = createTooltip("Enter the minimum and maximum percentage of G or C " +
                "nucleotides in a barcode here. The percentage of A and T nucleotides is inverse.");
        minGCField.setTooltip(gcTooltip);
        maxGCField.setTooltip(gcTooltip);

        // adds two options to the distance metric ChoiceBox
        distanceMetricChoiceBox.getItems().add("Levenshtein distance");
        distanceMetricChoiceBox.getItems().add("Hamming distance");
    }

    /**
     * Initializes all bindings
     */
    public void initBindings() {
        // add a listener to the barcode length field
        // ensuring that the barcode pattern is adapted to the length
		barcodeLengthField.valueProperty().addListener(
                (observableValue, oldValue, newValue) -> barcodePatternField.setLength(barcodeLengthField
                        .getValue()));

        // add a listener that ensures that the minimum is not larger
        // than the maximum
        minGCField.valueProperty().addListener((observableValue, oldValue, newValue) -> {

            if (minGCField.getValue() > maxGCField.getValue())
                maxGCField.setValue(minGCField.getValue());

        });

        // add a listener that ensures that the minimum is not smaller
        // than the minimum
        maxGCField.valueProperty().addListener((observableValue, oldValue, newValue) -> {

            if (minGCField.getValue() > maxGCField.getValue())
                minGCField.setValue(maxGCField.getValue());

        });

        // add a listener that sets the chosen distance metric
        distanceMetricChoiceBox.valueProperty().addListener((observableValue, oldItem, newItem) -> {
            if (newItem.toString().equals("Levenshtein distance")) {
                BarcodeDistanceCalculator.setDistanceType(1);
            } else {
                BarcodeDistanceCalculator.setDistanceType(0);
            }
        });
    }

    /* GETTER & SETTER */

    public Integer getMinGC() {
        return minGCField.getValue();
    }

    public Integer getMaxGC() {
        return maxGCField.getValue();
    }

    public String getBarcodePattern() {
        return barcodePatternField.getText();
    }

    public Integer getNumRandomBarcodes() {
        return numRandomBarcodesField.getValue();
    }

    public CheckBox getHammingCodeCheckBox() {
        return hammingCodeCheckBox;
    }
}
