package main.code.controller;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import main.code.ui.model.InfoWindowModel;

/**
 * The controller class for the info window
 *
 * @author Marietta Hamberger
 */
public class InfoWindowController {

    // label specifying the link to the institute's website
    public Label link;

    // label specifying info text "saved to clipboard"
    public Label infoClipboardLabel;

    private InfoWindowModel view;

    /**
     * Initializes the controller
     * @param infoWindowModel  The corresponding model class
     */
    public void init(InfoWindowModel infoWindowModel) {
        this.view = infoWindowModel;

        // initialize bindings
        initBindings();
    }

    /**
     * Initializes all bindings
     */
    private void initBindings() {

        // open quick info box about clipboard when user clicks on link
        // (instead of opening a browser)
        link.setOnMouseClicked(mouseEvent -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(link.getText());
            clipboard.setContent(content);
            infoClipboardLabel.setText("saved to clipboard");
            FadeTransition ft = new FadeTransition(Duration.millis(1000), infoClipboardLabel);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
        });
    }
}
