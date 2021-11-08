package main.code.ui.other;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import main.code.ui.model.WizardModel;

import java.io.IOException;

/**
 * Abstract base class for wizard pages
 * @author Christoph Muessel
 *
 */
public abstract class WizardPage {

	/**
	 * A string describing the content of the page
	 * that is shown at the top
	 */
	public StringProperty label;
	
	/**
	 * The label of the "Next" button
	 * (usually "Next")
	 */
	public StringProperty nextLabel;
	
	/**
	 * A Boolean property specifying whether the
	 * "Previous" button is currently disabled
	 */
	public BooleanProperty disablePrev;
	
	/**
	 * A Boolean property specifying whether the
	 * "Cancel" button is currently disabled
	 */
	public BooleanProperty disableCancel;
	
	/**
	 * A Boolean property specifying whether the
	 * "Next" button is currently disabled
	 */
	public BooleanProperty disableNext;
	
	/**
	 * The content of the wizard page
	 */
	protected Pane content;
	
	/**
	 * The wizard showing the page
	 */
	public WizardModel parent;
	
	/**
	 * The page shown before this page
	 */
	protected WizardPage prevPage;
	
	/**
	 * Returns the content pane of the wizard page
	 * @return	The content pane
	 */
	public Pane getContent() {
		return content;
	}

	/**
	 * Returns the wizard that displays this page
	 * @return	The parent wizard
	 */
	public WizardModel getParent() {
		return parent;
	}

	/**
	 * Sets the wizard that displays this page
	 * @param parent The new parent wizard
	 */
	public void setParent(WizardModel parent) {
		this.parent = parent;
	}	
	
	/**
	 * Returns the page displayed before this page
	 * @return The previous page
	 */
	public WizardPage getPrev() {
		return prevPage;
	}

	/**
	 * Sets the page displayed before this page
	 * @param prev	The new previous page
	 */
	public void setPrev(WizardPage prev) {
		this.prevPage = prev;
		if (prev != null)
			setParent(prev.getParent());
	}

	/**
	 * Obtains (and possibly creates) the next page
	 * @return	The next page
	 */
	public abstract WizardPage getNext() throws IOException;

	/**
	 * Creates a new wizard page
	 * @param content	The pane comprising the content of the page
	 * @param text		The info text to be shown at the top of the wizard page
	 * @param prev		The previous wizard page
	 */
	public WizardPage(Pane content, String text, WizardPage prev)
	{
		this.label = new SimpleStringProperty(text);
		this.nextLabel = new SimpleStringProperty("Next");
		this.disablePrev = new SimpleBooleanProperty(false);
		this.disableCancel = new SimpleBooleanProperty(false);
		this.disableNext = new SimpleBooleanProperty(false);
		this.content = content;
		this.setPrev(prev);
	}
	
	/**
	 * Determines if it is possible to move to the next page
	 * @return	true if it is possible to move to the next page,
	 * false otherwise
	 */
	public boolean processNextRequest() {
		return true;
	}

	/**
	 * Determines if it is possible to move to the previous page
	 * @return	true if it is possible to move to the previous page,
	 * false otherwise
	 */
	public boolean processPrevRequest() {
		return true;
	}
	
	/**
	 * Handles custom actions to be performed when the page is shown
	 */
	public void showPage() {
		
	}
}
