package main.code.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import main.code.ui.model.InfoWindowModel;
import main.code.ui.model.WizardModel;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The controller class for the (general) wizard page
 *
 * @author Marietta Hamberger
 */
public class WizardController {

    @FXML
    public GridPane mainWizardPane;

    /**
     * A label shown at the top of the wizard that comprises a description of
     * the current page
     */
    @FXML
    private Label descriptionLabel;

    /**
     * The pane holding the current wizard page
     */
    @FXML
    private AnchorPane contentPane;

    /**
     * The pane holding the button bar and the info label
     */
    @FXML
    public GridPane bottomPane;

    /**
     * A clickable label that displays the info dialog
     */
    @FXML
    private Label infoLabel;

    /**
     * A button to navigate to the next page
     */
    @FXML
    private Button nextButton;

    /**
     * A button to navigate to the previous page
     */
    @FXML
    private Button prevButton;

    /**
     * A button to terminate the wizard and close its window
     */
    @FXML
    private Button cancelButton;

    private WizardModel view;

    /**
     * Initializes the controller
     * @param wizardModel  The corresponding model class
     */
    public void init(WizardModel wizardModel) {
        this.view = wizardModel;

        // initialize bottom pane
        initBottomPane();
    }

    /**
     * Initializes the bottom pane - encompasses buttons and info
     */
    private void initBottomPane() {
        // make label clickable and show info dialog
        infoLabel.setOnMouseClicked(arg0 -> {
            InfoWindowModel info;
            try {
                info = new InfoWindowModel(view.getMainWizardPane().getScene());
                info.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // initialize button actions
        prevButton.setOnAction(event -> {
            // add a slight delay to make sure
            // all delayed updates of controls have been processed
            // before moving to the previous page
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> view.prevPage());
                }
            }, 500);
        });

        cancelButton.setOnAction(event -> ((Stage) view.getMainWizardPane().getScene().getWindow()).close());

        nextButton.setOnAction(event -> {
            // add a slight delay to make sure
            // all delayed updates of controls have been processed
            // before moving to the next page
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            view.nextPage();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }, 500);

        });
    }

    /* GETTER & SETTER */
    public Button getNextButton() {
     	return nextButton;
    }

    public Button getPrevButton() {
     	return prevButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public AnchorPane getContentPane() {
        return contentPane;
    }

    public Label getDescriptionLabel() {
        return descriptionLabel;
    }
}
