package main.code.ui.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import main.code.controller.ProgressController;
import main.code.ui.helpers.ResourceLoader;
import main.code.ui.other.WizardPage;

import java.io.IOException;

/**
 * Generic progress wizard page that attaches a progress indicator and a message label to a
 * task that encapsulates a process
 * 
 * @author Christoph Muessel, Marietta Hamberger
 *
 * @param <T>
 *            The task class describing the process to be executed
 */
public class ProgressModel<T> extends WizardPage {
	
	/**
	 * The task that is executed by the page
	 */
	private Task<T> task;


	/**
	 * The page to be shown after task completion
	 */
	private WizardPage nextPage;

	/**
	 * Returns the task attached to this window.
	 * 
	 * @return The task
	 */
	public Task<T> getTask() {
		return task;
	}

	/**
	 * The return value of the task. Available when the task completed
	 * successfully.
	 */
	public ObjectProperty<T> returnValue;

	/**
	 * The controller responsible for converting user input into commands for model or model
	 */
	private ProgressController controller;


	/**
	 * Creates a progress wizard page.
	 * 
	 * @param text
	 *          The text to show on top of the window
	 * @param prev
	 * 			The previous wizard page
	 * @param next
	 * 			The next wizard page
	 * @param task
	 *            The task that is synchronized with the progress indicator
	 */
	public ProgressModel(String text, WizardPage prev, WizardPage next, Task<T> task) throws IOException {
		super(new GridPane(), text, prev);

		// initialize variables
		this.nextPage = next;
		this.task = task;
		this.returnValue = new SimpleObjectProperty<>();

		// initialize content pane
		FXMLLoader tmpLoader = ResourceLoader.getFXML("progressView");
		GridPane contentPane = tmpLoader.load();
		controller = tmpLoader.getController();
		controller.init(this);
		this.content = contentPane;

		this.task.setOnSucceeded(event -> {
			// set return value when the task completed
			returnValue.set(getTask().getValue());
		});

		this.task.setOnCancelled(event -> {
			// go to previous page if the user cancelled the process
			parent.prevPage();
		});

		this.task.setOnFailed(event -> {
			// show the exception message when the task failed,
			// and go to previous page
			new MessageWindowModel(contentPane.getScene(),
					getTask().getException().getMessage(), true);
			parent.prevPage();
		});

		// initialize controller
		controller.initBindings();

		// all buttons deactivated
		disablePrev.set(true);
		disableCancel.set(true);
		disableNext.set(true);
	}

	/**
	 * Show the next page
	 */
	@Override
	public void showPage() {
		// start task
		new Thread(task).start();		
	}

	/**
	 * Get the next page
	 * @return nextPage
	 */
	@Override
	public WizardPage getNext() {
		return nextPage;
	}

}
