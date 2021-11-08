package main.code.ui.controls;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

/**
 * A popup panel showing the color balance for a barcode set
 * in form of red and green bars
 * @author Christoph Muessel
 *
 */
public class ColorBalancePopup extends Popup {
	/**
	 * An array storing the percentage of A/C nucleotides
	 * at each position
	 */
	private double[] acPercentage = null;
	/**
	 * The canvas on which the bars are drawn
	 */
	private Canvas canvas;
	/**
	 * The main panel of the popup window
	 */
	private VBox mainPane;
	/**
	 * The caption label of the visualization
	 */
	private Label captionLabel;

	/**
	 * The color to be used for the 50% line
	 */
	private static Color COLOR_LINE = new Color(0, 0, 1, 0.8);
	/**
	 * The color to be used for the fraction of "red" nucleotides
	 */
	private static Color COLOR_RED = new Color(0.9, 0.0, 0.0, 0.8);
	/**
	 * The color to be used for the fraction of "green" nucleotides
	 */	
	private static Color COLOR_GREEN = new Color(0.0, 0.9, 0.0, 0.8);

	public ColorBalancePopup(String[] barcodes, int width, int height) {
		if (barcodes.length > 0) {
			// calculate the percentage of A/C nucleotides at each position
			acPercentage = new double[barcodes[0].length()];

			for (int i = 0; i < barcodes[0].length(); ++i)
				acPercentage[i] = 0.0;

			for (String barcode : barcodes)
				for (int i = 0; i < barcode.length(); ++i) {
					if (barcode.charAt(i) == 'A' || barcode.charAt(i) == 'C')
						acPercentage[i]++;
				}

			for (int i = 0; i < barcodes[0].length(); ++i)
				acPercentage[i] /= barcodes.length;

		}
		this.setAutoHide(true);
		mainPane = new VBox(5);
		mainPane.setPadding(new Insets(5, 5, 5, 5));
		
		canvas = new Canvas();
		canvas.setWidth(width - 10);
		canvas.setHeight(height - 10);
		canvas.setMouseTransparent(true);

		mainPane.setStyle("-fx-border-color: black; -fx-background-color: #000000cd");
		mainPane.setMouseTransparent(true);
		
		captionLabel = new Label("Color balance:");
		captionLabel.setStyle("-fx-font: 10px \"Dialog\"; -fx-text-fill: white");
		
		mainPane.getChildren().add(captionLabel);
		mainPane.getChildren().add(canvas);
		HBox.setHgrow(mainPane, Priority.ALWAYS);		

		this.getContent().add(mainPane);
		redraw();

		// redraw content when resized
		InvalidationListener listener = observable -> redraw();
		canvas.widthProperty().addListener(listener);
		canvas.heightProperty().addListener(listener);

	}

	/**
	 * Redraws the color balance chart
	 */
	private void redraw() {
		if (acPercentage != null) {
			GraphicsContext con = canvas.getGraphicsContext2D();
			con.setLineWidth(1.0);
			// determine the width of each nucleotide position
			double colWidth = (canvas.getWidth() / acPercentage.length);
			for (int i = 0; i < acPercentage.length; ++i) {
				// determine the height of the bar for A/C nucleotides
				// at the current position
				double acLen = (acPercentage[i] * canvas.getHeight());

				// draw bar for A/C nucleotides
				con.setStroke(Color.LIGHTGREY);
				con.setFill(COLOR_RED);
				con.fillRect(i * colWidth, 0, colWidth, acLen);
				con.strokeRect(i * colWidth, 0, colWidth, acLen);

				// draw complementary bar for G/T nucleotides 
				con.setFill(COLOR_GREEN);
				con.fillRect(i * colWidth, acLen, colWidth, canvas.getHeight()
						- acLen);
				con.strokeRect(i * colWidth, acLen, colWidth,
						canvas.getHeight() - acLen);
			}
			
			// draw 50% line as dashed line
			con.setStroke(COLOR_LINE);		    
		    for (double x=0; x <= canvas.getWidth() - 2; x += 6) {
		        con.strokeLine(x, 0.5 * canvas.getHeight(), x+2, 0.5 * canvas.getHeight());
		    }

		}
	}

}
