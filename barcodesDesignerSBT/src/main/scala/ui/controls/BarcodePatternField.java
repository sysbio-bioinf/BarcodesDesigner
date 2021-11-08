package main.code.ui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A text field of a fixed length 
 * that only allows for entering the nucleotide
 * characters A/C/G/T and _

 * @author Christoph Muessel
 *
 */
public class BarcodePatternField extends TextField {
	private int length;

	/**
	 * Sets the number of nucleotides in the barcode
	 * @param newValue The new barcode length
	 */
	public void setLength(int newValue) {
		if (newValue < this.length)
			// extract the first characters when the barcode gets shorter
			this.textProperty().setValue(
					this.textProperty().getValue().substring(0, newValue));
		else if (newValue > this.length) {
			// add "_" characters when the barcode grows longer.
			StringBuilder s = new StringBuilder(this.textProperty().getValue());
			s.append("_".repeat(Math.max(0, newValue - this.length)));
			this.textProperty().setValue(s.toString());
		}
		this.length = newValue;
	}

	/**
	 * Creates a new barcode pattern text field consisting of "_" characters.
	 * @param length The number of nucleotides in the barcode
	 */
	public BarcodePatternField(int length) {
		this.length = length;
		
		// align right, and use fixed-width font
		this.setAlignment(Pos.BASELINE_RIGHT);
		this.setStyle("-fx-font-family: \"Courier New\"");

		// create initial barcode
		this.textProperty().setValue(generateBarcodePattern(this.length));

		final BarcodePatternField bcField = this;

		this.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
			// handle backspace and delete by replacing the
			// current characters by "_" symbols
			int pos = bcField.caretPositionProperty().get();

			if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
				if (pos > 0)
					bcField.replaceText(pos-1, pos, "_");
				bcField.positionCaret(pos - 1);
				keyEvent.consume();
			}
			if (keyEvent.getCode() == KeyCode.DELETE) {
				if (pos < bcField.textProperty().getValue().length())
					bcField.replaceText(pos, pos + 1, "_");
				keyEvent.consume();
			}
		});

		this.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {

			// ensure that only A/C/G/T and _ are entered
			int pos = bcField.caretPositionProperty().get();
			String s = keyEvent.getCharacter().toUpperCase();
			if (s.matches("[ACGT_]")
					&& pos < bcField.textProperty().getValue().length())
				bcField.replaceText(pos, pos + 1, s);

			keyEvent.consume();
		});
	}

	/**
	 * Generates the barcode pattern string from the given length
	 * @param length length of obtained barcode pattern
	 * @return
	 */
	public static String generateBarcodePattern(int length){
		StringBuilder s = new StringBuilder();
		s.append("_".repeat(Math.max(0, length)));
		return s.toString();
	}

}
