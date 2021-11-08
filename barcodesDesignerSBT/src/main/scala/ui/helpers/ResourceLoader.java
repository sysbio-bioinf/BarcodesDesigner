package main.code.ui.helpers;

import javafx.fxml.FXMLLoader;
import java.io.InputStream;

/**
 * Helper class to load resources
 * @author Marietta Hamberger
 */
public class ResourceLoader {

    /**
     * Loads a fxml file and returns the FXMLLoader object
     * @param fxml The fxml file name without file ending
     * @return A FXMLLoader object
     */
    public static FXMLLoader getFXML(String fxml) {
        return (new FXMLLoader(ResourceLoader.class.getResource("/fxml/" + fxml + ".fxml")));
    }

    /**
     * Generates an input stream from an image file
     * @param imgName The file name incl. file ending
     * @return InputStream for the image
     */
    public static InputStream loadImg(String imgName) {
        return ResourceLoader.class.getResourceAsStream("/img/" + imgName);
    }
}
