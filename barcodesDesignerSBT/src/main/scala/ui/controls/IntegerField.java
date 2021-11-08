package main.code.ui.controls;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Helper text field subclass which restricts text input to a given range of
 * natural int numbers 
 * 
 * Copied and adapted from
 * https://gist.github.com/jewelsea/1962045
 */
public class IntegerField extends TextField {
	@FXML
	final private IntegerProperty value;
	@FXML
	private final int minValue;
	@FXML
	private final int maxValue;

	/**
	 * Gets the integer value associated with the field
	 * @return The field's value
	 */
	public int getValue() {
		return value.getValue();
	}

	/**
	 * Sets the integer value of the field
	 * @param newValue	The new value
	 */
	public void setValue(int newValue) {
		value.setValue(newValue);
	}

	/**
	 * Gets the integer property object
	 * @return The property object
	 */
	public IntegerProperty valueProperty() {
		return value;
	}

	/**
	 * Creates a new integer field with the specified parameters.
	 * @param minValue	The minimum value the field can take
	 * @param maxValue	The maximum value the field can take
	 * @param initialValue	The initial value of the field
	 */
	public IntegerField(int minValue, int maxValue, int initialValue) {
		if (minValue > maxValue)
			throw new IllegalArgumentException("IntField min value " + minValue
					+ " greater than max value " + maxValue);
		if (!((minValue <= initialValue) && (initialValue <= maxValue)))
			throw new IllegalArgumentException("IntField initialValue "
					+ initialValue + " not between " + minValue + " and "
					+ maxValue);
		// initialize the field values.
		this.minValue = minValue;
		this.maxValue = maxValue;

		this.setAlignment(Pos.BASELINE_RIGHT);
		this.setPrefColumnCount((maxValue + "").length());
		this.setMinWidth(Region.USE_PREF_SIZE);
		this.setMaxWidth(Region.USE_PREF_SIZE);

		value = new SimpleIntegerProperty(initialValue);
		setText(initialValue + "");

		final IntegerField intField = this;
		// make sure the value property is clamped to the required range
		// and update the field's text to be in sync with the value.
		value.addListener((observableValue, oldValue, newValue) -> {
			if (newValue == null) {
				intField.setText("");
			} else {
				if (newValue.intValue() < intField.minValue) {
					value.setValue(intField.minValue);
					return;
				}
				if (newValue.intValue() > intField.maxValue) {
					value.setValue(intField.maxValue);
					return;
				}
				if (!(newValue.intValue() == 0
						&& (textProperty().get() == null || ""
								.equals(textProperty().get())))) {
					if (!intField.getText().equals(newValue.toString()))
						intField.setText(newValue.toString());
				}
			}
		});
		// restrict key input to numerals.
		this.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {

			if (! "0123456789".contains(keyEvent.getCharacter())) {
				keyEvent.consume();
			}
		});
		// ensure any entered values lie inside the required range.
		this.textProperty().addListener((observableValue, oldValue, newValue) -> {

			// Add a slight delay to allow for making some more complex changes
			// before the input is validated and potentially discarded
			final Timer timer = new Timer();
			final String oldVal = oldValue;
			timer.schedule(new TimerTask() {
				public void run() {
					Platform.runLater(() -> {
						String newVal = getText();
						if (newVal == null || "".equals(newVal)) {
							return;
						}
						final int intValue = Integer.parseInt(newVal);
						if (intField.minValue > intValue
								|| intValue > intField.maxValue) {
							textProperty().setValue(oldVal);
						}
						value.set(Integer.parseInt(newVal));
						timer.cancel();
					});
				}
			}, 500, 500);
		});

		this.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
			// ensure that the field has a valid value when the focus is lost
			if (!newPropertyValue
					&& intField.getText().trim().length() == 0) {
				intField.setText(intField.minValue + "");
			}
		});
	}
}
