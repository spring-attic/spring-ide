package org.springframework.ide.eclipse.beans.core.model;

import java.util.EventObject;


/**
 * An element changed event describes a change to an element of the bean model.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the Beans model.
 * </p>
 * @see IBeansModelChangedListener
 */
public class BeansModelChangedEvent extends EventObject {

	public static final int ADDED = 1;
	public static final int REMOVED = 2;
	public static final int CHANGED = 3;

	private int type;

	/**
	 * Creates an new element changed event (based on a
	 * <code>IBeansElementDelta</code>).
	 *
	 * @param element  the Beans element delta.
	 * @param type  the type of modification (ADDED, REMOVED, CHANGED) this
	 * 				event contains
	 */
	public BeansModelChangedEvent(IBeansModelElement element, int type) {
		super(element);
		this.type = type;
	}

	/**
	 * Returns the modified element.
	 */
	public IBeansModelElement getElement() {
		return (IBeansModelElement) getSource();
	}

	/**
	 * Returns the type of modification.
	 */
	public int getType() {
		return type;
	}
}
