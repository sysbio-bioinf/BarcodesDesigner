package main.code.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import main.code.commandLineUse.CommandLineMain;
import main.code.ui.model.ModeChoiceModel;
import main.code.ui.model.WizardModel;

import java.util.Objects;

/**
 * The main graphical user interface
 * @author Marietta Hamberger, Christoph Müssel
 *
 */
public class App extends Application {

    private Scene scene;

    /**
     * Launches the application
     * @param args
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            CommandLineMain.main(args);
        } else {
            launch(args);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // set the stage
        primaryStage
                .getIcons()
                .add(new Image(Objects.requireNonNull(
                        App.class.getResourceAsStream("/img/icon02.png"))));

        // generate the first page
        WizardModel w = new WizardModel(new ModeChoiceModel());

        // set the scene
        scene = new Scene(w.getMainWizardPane(),600,400);

        // set the stage
        primaryStage.setOnHiding(event -> {
            try {
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        primaryStage.setTitle("Barcode Designer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);

        // show the stage
        primaryStage.show();
    }
}
