package main.code.commandLineUse;

public class CommandLineHelpers {

    /**
     * Generates barcode pattern string
     * @param length length of the obtained barcode pattern
     * @return barcode pattern
     */
    public static String generateBarcodePattern(int length){
        return "_".repeat(Math.max(0, length));
    }

}
