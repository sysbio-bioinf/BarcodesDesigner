package main.code.ui.other;

/**
 * An enumeration encapsulating the algorithm choices
 * @author Christoph Muessel
 *
 */
public enum WizardMode {
    /**
     * Generate barcode sets of fixed size
     */
    GENERATE_SETS,
    /**
     * Select subsets from an initial barcode set
     */
    SELECT_SETS,
    /**
     * Load barcode sets from files
     */
    LOAD_RESULTS
}