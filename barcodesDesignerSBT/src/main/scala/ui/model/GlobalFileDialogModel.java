package main.code.ui.model;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import main.code.algorithm.BarcodeReader;
import main.code.algorithm.BarcodeSet;
import main.code.algorithm.BarcodeSetCollection;
import main.code.algorithm.SimpleBarcodeSet;
import java.io.File;

/**
 * A static helper class providing a file chooser
 * for text files and BarcodeDesigner JSON files
 * @author Christoph Muessel
 *
 */
public class GlobalFileDialogModel {
	public static FileChooser fileChooser = new FileChooser();

	/**
	 * Internal method that sets the file type filters for the file chooser
	 * appropriately depending on the context
	 *
	 * @param allowText
	 *            Specifies whether text files are allowed
	 * @param allowJSON
	 *            Specifies whether JSON files (.bdj) are allowed
	 */
	public static void setExtensionFilter(boolean allowText, boolean allowJSON) {
		fileChooser.getExtensionFilters().clear();

		if (allowJSON)
			fileChooser.getExtensionFilters().add(
					new ExtensionFilter("Barcode Designer files (*.bdj)",
							"*.bdj"));

		if (allowText)
			fileChooser.getExtensionFilters().add(
					new ExtensionFilter("Plain text files (*.txt)", "*.txt"));
		fileChooser.getExtensionFilters().add(
				new ExtensionFilter("All files (*.*)", "*.*"));
	}

	/**
	 * Shows a file chooser, and loads the chosen barcodes from a text file
	 * @param stage	The parent stage
	 * @return	The barcodes in the file
	 */
	public static String[] chooseAndLoadBarcodes(Stage stage){
		setExtensionFilter(true, false);
		File file = fileChooser.showOpenDialog(stage);
		String[] barcodes;
		if (file == null)
			return null;

		try {
			// load the barcodes using Scala class
			barcodes = BarcodeReader.readBarcodes(file
					.getAbsolutePath());
		} catch (Exception ex) {
			new MessageWindowModel(stage.getScene(), ex.getMessage(), true);
			return null;
		}
		return barcodes;
	}

	/**
	 * Shows a file chooser, and loads a collection of barcode sets from the specified
	 * text file or BarcodeDesigner JSON file
	 * @param stage	The parent stage
	 * @return	The collection of barcode sets in the file
	 */
	public static BarcodeSetCollection chooseAndLoadBarcodeSets(Stage stage){
		setExtensionFilter(true, true);
		File file = fileChooser.showOpenDialog(stage);
		if (file == null)
			return null;

		BarcodeSetCollection result;
		try {

			String extension;
			int i = file.getName().lastIndexOf('.');
			if (i > 0) {
				extension = file.getName().substring(i + 1)
						.toLowerCase();
			} else
				extension = "";

			if (extension.equals("bdj"))
				// load multiple sets from JSON
				result = new BarcodeSetCollection(file.getAbsolutePath());
			else
			// load single set from text file
			{
				String [] barcodes = BarcodeReader.readBarcodes(file
						.getAbsolutePath());
				result = new BarcodeSetCollection(new BarcodeSet[]{new SimpleBarcodeSet(barcodes)});
			}
		} catch (Exception ex) {
			new MessageWindowModel(stage.getScene(), ex.getMessage(), true);
			return null;
		}
		return result;
	}

}

