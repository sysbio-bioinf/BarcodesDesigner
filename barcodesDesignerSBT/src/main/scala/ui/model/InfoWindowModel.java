package main.code.ui.model;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.code.controller.InfoWindowController;
import main.code.ui.helpers.ResourceLoader;

import java.io.IOException;

/**
 * A window class showing an information dialog
 * @author Marietta Hamberger, Christoph Muessel
 *
 */
public class InfoWindowModel extends Stage {

	/**
	 * The scene of the window
	 */
	private Scene modalScene;

	private final Scene parent;

	InfoWindowController controller;

	AnchorPane mainAnchor;

	/**
	 * Creates a new modal information window
	 * @param parent The parent scene
	 */
	public InfoWindowModel(Scene parent) throws IOException {
		// initialize content pane
		this.parent = parent;
		FXMLLoader tmpLoader = ResourceLoader.getFXML("infoWindowView");
		mainAnchor = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
		initStage();

	}

	public void initStage() {
		modalScene = new Scene(mainAnchor, 400, 280);
		this.setScene(modalScene);
		this.setResizable(false);
		this.initModality(Modality.WINDOW_MODAL);
		//this.initStyle(StageStyle.UNDECORATED);
		this.initOwner(parent.getWindow());
		this.setTitle("Info");
	}
}
