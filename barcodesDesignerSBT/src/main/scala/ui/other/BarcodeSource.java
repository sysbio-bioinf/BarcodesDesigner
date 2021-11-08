package main.code.ui.other;

/**
 * An enumeration specifying the chosen option
 * for the initial barcode set
 * @author Christoph Muessel
 *
 */
public enum BarcodeSource {
    /**
     * Generate the initial barcode set at random
     */
    RANDOM_GENERATION,
    /**
     * Load the initial barcode set from a file
     */
    LOAD_FILE
}