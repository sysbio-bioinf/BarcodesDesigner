package main.code.ui.model;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A window class showing a message dialog
 * 
 * @author Christoph Muessel
 *
 */
public class MessageWindowModel extends Stage {

	/** 
	 * The label with the message to show
	 */
	private Label infoLabel;
	/**
	 * The main panel of the window
	 */
	private VBox mainPane;
	/**
	 * The scene for the window
	 */
	private Scene modalScene;
	/**
	 * The "OK" button to close the window
	 */
	private Button closeButton;

	/**
	 * Creates a new modal message box
	 * 
	 * @param parent
	 *            The parent scene
	 */
	public MessageWindowModel(Scene parent, String message, boolean error) {
		mainPane = new VBox(5);
		mainPane.setAlignment(Pos.CENTER);
		mainPane.setPadding(new Insets(20,20,20,20));

		modalScene = new Scene(mainPane, 300, 150);
		infoLabel = new Label(message);		
		
		if (error){
			// show message in red
			infoLabel = new Label("An error occurred: " + message);
			infoLabel.setStyle("-fx-text-fill: #FF0000");			
		}
		else
			infoLabel = new Label(message);
		
		infoLabel.setWrapText(true);				
		infoLabel.setPrefSize(280, 100);
		mainPane.getChildren().add(infoLabel);
		VBox.setVgrow(infoLabel, Priority.ALWAYS);		
		HBox.setHgrow(infoLabel, Priority.ALWAYS);

		closeButton = new Button("OK");
		closeButton.setOnAction(event -> close());
		mainPane.getChildren().add(closeButton);
		
		this.setScene(modalScene);
		this.setResizable(false);
		this.initModality(Modality.WINDOW_MODAL);
		this.initStyle(StageStyle.UNDECORATED);
		this.initOwner(parent.getWindow());
		this.setTitle("Info");
		this.showAndWait();
	}
}
