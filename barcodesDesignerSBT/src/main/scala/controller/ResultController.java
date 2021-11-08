package main.code.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import main.code.algorithm.BarcodeReader;
import main.code.algorithm.BarcodeSet;
import main.code.algorithm.BarcodeSetCollection;
import main.code.ui.controls.BarcodeTreeCell;
import main.code.ui.model.GlobalFileDialogModel;
import main.code.ui.model.MessageWindowModel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * The controller class for the results page
 *
 * @author Marietta Hamberger
 */
public class ResultController {

    /**
     * A gridpane holding the content of the barcode generation page
     */
    @FXML
    public GridPane contentPane;

    /**
     * The result tree containing the barcode sets
     */
    @FXML
    public TreeView<Object> resultTree;

    /**
     * The dummy root node of the tree containing children that correspond to
     * barcode sets
     */
    private TreeItem<Object> rootResultNode;

    /**
     * A context menu for the result tree that allows for saving and copying
     * barcode sets
     */
    @FXML
    private ContextMenu resultMenu;

    /**
     * A menu item that allows for saving single barcode sets to text files
     */
    private MenuItem saveSingleSetMenuItem;

    /**
     * A menu item that allows for saving all barcode sets to a single text file
     */
    private MenuItem saveAllSetsMenuItem;

    /**
     * Initializes the controller/the page
     */
    public void init(){
        initTree();
        initContextMenu();
        initTreeBindings();
    }

    /**
     * Initializes the TreeView holding the results
     */
    private void initTree() {
        rootResultNode = new TreeItem<>(null);
        resultTree.setRoot(rootResultNode);

        // add a custom cell factory that generates tree cells which show the
        // balance of colors at the nucleotide positions in a popup window
        resultTree.setCellFactory(arg0 -> new BarcodeTreeCell());
    }

    /**
     * Initializes the TreeView's bindings
     */
    private void initTreeBindings() {
        resultTree.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> saveSingleSetMenuItem.setVisible(newValue != null
                        && newValue.getValue() instanceof BarcodeSet));
    }

    /**
     * Initializes the context menu
     */
    private void initContextMenu() {
        resultMenu = new ContextMenu();

        // A menu item that allows to save the selected barcode set
        saveSingleSetMenuItem = new MenuItem("Save barcode set");
        saveSingleSetMenuItem.setOnAction(event -> {
            // show save dialog for text files
            GlobalFileDialogModel.setExtensionFilter(true, true);
            File file = GlobalFileDialogModel.fileChooser.showSaveDialog(contentPane.getScene().getWindow());
            if (file != null) {
                // check for file extension
                String extension;
                int i = file.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = file.getName().substring(i + 1)
                            .toLowerCase();
                } else
                    extension = "";

                // check whether a selection must be added to the file name
                // (only if not "All files" has been selected)
                if (extension.equals("")) {
                    String selectedExtension = GlobalFileDialogModel.fileChooser
                            .getSelectedExtensionFilter().getExtensions()
                            .get(0).substring(2);
                    if (!selectedExtension.equals("*")) {
                        file = new File(file.getAbsolutePath().concat(".")
                                .concat(selectedExtension));
                        extension = selectedExtension;
                    }
                }

                if (resultTree.getSelectionModel().getSelectedIndices().isEmpty()
                        && (!resultTree.getRoot().getChildren().isEmpty())) {
                    resultTree.getSelectionModel().select(0);
                }
                // selected barcode set is attached to the tree item
                BarcodeSet set = (BarcodeSet) resultTree
                        .getSelectionModel().getSelectedItem().getValue();

                if (!resultTree.getRoot().getChildren().isEmpty()) {
                    if (extension.equals("bdj")) {
                        // export single barcode set to JSON
                        PrintWriter output;
                        try {
                            output = new PrintWriter(file);
                            BarcodeSetCollection res = (BarcodeSetCollection) rootResultNode
                                    .getValue();
                            output.print(res
                                    .toJSON(new BarcodeSet[]{set}));
                            output.close();
                        } catch (FileNotFoundException ex) {
                            new MessageWindowModel(contentPane.getScene(), ex.getMessage(), true);
                        }
                    } else {
                        // export single barcode set to a plain text file
                        BarcodeReader.writeBarcodes(file.getAbsolutePath(),
                                set.getBarcodes());
                    }
                }

            }
        });
        resultMenu.getItems().add(saveSingleSetMenuItem);

        // A menu item that allows to save all barcodes
        saveAllSetsMenuItem = new MenuItem("Save all barcode sets");

        EventHandler<ActionEvent> saveAllResultsHandler = event -> {
            // show save dialog for text files
            GlobalFileDialogModel.setExtensionFilter(true, true);
            File file = GlobalFileDialogModel.fileChooser.showSaveDialog(contentPane.getScene().getWindow());

            if (file != null) {
                // check for file extension
                String extension;
                int i = file.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = file.getName().substring(i + 1)
                            .toLowerCase();
                } else
                    extension = "";

                // check whether an extension must be added to the file name
                // (only if not "All files" has been selected)
                if (extension.equals("")) {
                    String selectedExtension = GlobalFileDialogModel.fileChooser
                            .getSelectedExtensionFilter().getExtensions()
                            .get(0).substring(2);
                    if (!selectedExtension.equals("*")) {
                        file = new File(file.getAbsolutePath().concat(".")
                                .concat(selectedExtension));
                        extension = selectedExtension;
                    }
                }

                PrintWriter output;
                try {
                    output = new PrintWriter(file);
                    BarcodeSetCollection res = (BarcodeSetCollection) rootResultNode
                            .getValue();
                    if (extension.equals("bdj"))
                        // export as JSON
                        output.print(res.toJSON());
                    else
                        // export as plain text
                        output.print(res.toString());
                    output.close();
                } catch (FileNotFoundException ex) {
                    new MessageWindowModel(contentPane.getScene(), ex.getMessage(), true);
                }
            }
        };

        saveAllSetsMenuItem.setOnAction(saveAllResultsHandler);
        resultMenu.getItems().add(saveAllSetsMenuItem);

         // A menu item that allows for copying barcode sets or single barcodes to
         // the clipboard
        MenuItem copyResultsMenuItem = new MenuItem("Copy to clipboard");
        copyResultsMenuItem.setOnAction(event -> {
            if (resultTree.getSelectionModel().getSelectedIndices().isEmpty()
                    && (!resultTree.getRoot().getChildren().isEmpty())) {
                resultTree.getSelectionModel().select(0);
            }

            if (!resultTree.getRoot().getChildren().isEmpty()) {
                Object selected = resultTree.getSelectionModel()
                        .getSelectedItem().getValue();
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();

                if (selected instanceof BarcodeSet) {
                    // the selected item is a set => create list of barcodes
                    // separated by newline
                    StringBuilder s = new StringBuilder();
                    for (String barcode : ((BarcodeSet) selected).getBarcodes()) {
                        s.append(barcode);
                        s.append("\n");
                    }
                    content.putString(s.toString());
                } else {
                    // the selected item is a single barcode => copy it directly
                    // to the clipboard
                    content.putString(selected.toString());
                }

                clipboard.setContent(content);
            }

        });
        resultMenu.getItems().add(copyResultsMenuItem);
        resultTree.setContextMenu(resultMenu);
    }

    /**
     * Updates the result tree with new optimization results
     *
     * @param result  A collection of barcode sets to be displayed
     */
    public void updateResults(BarcodeSetCollection result) {
        // remove previous results
        rootResultNode.getChildren().clear();
        rootResultNode.setValue(result);

        // add a top-level node for each set with one
        // child for each comprised barcode
        for (BarcodeSet set : result.sets()) {
            TreeItem<Object> setNode = new TreeItem<>(set);
            rootResultNode.getChildren().add(setNode);

            String[] barcodes = set.getBarcodes();

            for (String barcode : barcodes) {
                setNode.getChildren().add(new TreeItem<>(barcode));
            }
        }
        //changed
        saveAllSetsMenuItem.setVisible(result.sets().length > 1);
    }


}
