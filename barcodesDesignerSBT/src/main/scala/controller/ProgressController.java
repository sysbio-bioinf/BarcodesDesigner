package main.code.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import main.code.ui.model.ProgressModel;

/**
 * The controller class for the progress page
 *
 * @author Marietta Hamberger
 */
public class ProgressController {

    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    public GridPane contentPane;

    /**
     * The progress indicator showing the progress of the task
     */
    @FXML
    public ProgressIndicator progress;

    /**
     * A pane containing the message label
     */
    @FXML
    public AnchorPane labelPane;

    /**
     * A label displaying progress messages from the task
     */
    @FXML
    public Label messageLabel;

    /**
     * A button to interrupt the task
     */
    @FXML
    public Button cancelButton;

    private ProgressModel<?> view;

    /**
     * Initializes the controller
     * @param progressModel  The corresponding model class
     */
    public void init(ProgressModel<?> progressModel){
        view = progressModel;

        // initialize the cancel button
        cancelButton.setOnAction(event -> view.getTask().cancel());
    }

    /**
     * Initialize all bindings
     */
    public void initBindings() {
        // bind progress indicator and message label to the task
        progress.progressProperty().bind(view.getTask().progressProperty());
        messageLabel.textProperty().bind(view.getTask().messageProperty());
    }

    /**
     * Get current progress (for visualization)
     */
    public ProgressIndicator getProgress() {
        return progress;
    }
}
