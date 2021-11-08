package main.code.ui.helpers;

import javafx.scene.control.Tooltip;

public class OtherHelpers {

    /**
     * Helper function that creates a word-wrapping tooltip with a width of 20px.
     *
     * @param text The tooltip text
     * @return The tooltip object
     */
    public static Tooltip createTooltip(String text) {
        Tooltip tip = new Tooltip(text);
        tip.setWrapText(true);
        tip.setPrefWidth(200);
        tip.setStyle("-fx-border-color: black; -fx-background-color: #000000cd; -fx-font: 10px \"Dialog\"; -fx-text-fill: white");
        // tip.setFont(new Font("System",14));
        return tip;
    }
}
