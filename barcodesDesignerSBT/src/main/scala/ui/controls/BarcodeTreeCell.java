package main.code.ui.controls;

import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;
import main.code.algorithm.BarcodeSet;

/**
 * A tree cell class for barcode sets that shows a popup displaying the color
 * balance of the set at each nucleotide position
 *
 * @author Christoph Muessel, Marietta Hamberger
 *
 */
public class BarcodeTreeCell extends TreeCell<Object> {
    private ColorBalancePopup popup = null;

    public BarcodeTreeCell() {
        super();

        // show popup when the mouse is moved over the node
        this.addEventFilter(MouseEvent.MOUSE_ENTERED,
                event -> {
                    if (popup != null) {
                        popup.setX(event.getScreenX() + 10);
                        popup.setY(event.getScreenY());
                        popup.show(getScene().getWindow());
                    }

                });

        // hide popup when the mouse exits the node
        this.addEventFilter(MouseEvent.MOUSE_EXITED,
                event -> {
                    if (popup != null) popup.hide();
                });

    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        popup = null;
        if (!empty) {
            // set display text
            setText(item.toString());
            if (item instanceof BarcodeSet) {
                // create popup window for the barcode set
                popup = new ColorBalancePopup(
                        ((BarcodeSet) item).getBarcodes(), 200, 80);
            }
        } else {
            graphicProperty().setValue(null);
            setText(null);
        }
    }

}