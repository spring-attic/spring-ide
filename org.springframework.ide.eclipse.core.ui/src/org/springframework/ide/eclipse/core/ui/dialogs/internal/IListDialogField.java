/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.ui.dialogs.internal;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.core.ui.fields.IDialogField;

/**
 * Description of a List Dialog Field
 * @author Pierre-Antoine Grégoire
 */
public interface IListDialogField extends IDialogField {

	/**
	 * Sets the index of the 'remove' button in the button label array passed in the constructor. The behaviour of the button marked as the 'remove' button will then be handled internally. (enable state, button invocation behaviour)
	 */
	public abstract void setRemoveButtonIndex(int removeButtonIndex);

	/**
	 * Sets the index of the 'up' button in the button label array passed in the constructor. The behaviour of the button marked as the 'up' button will then be handled internally. (enable state, button invocation behaviour)
	 */
	public abstract void setUpButtonIndex(int upButtonIndex);

	/**
	 * Sets the index of the 'down' button in the button label array passed in the constructor. The behaviour of the button marked as the 'down' button will then be handled internally. (enable state, button invocation behaviour)
	 */
	public abstract void setDownButtonIndex(int downButtonIndex);

	/**
	 * Sets the viewerSorter.
	 * 
	 * @param viewerSorter The viewerSorter to set
	 */
	public abstract void setViewerSorter(ViewerSorter viewerSorter);

	

	/*
	 * @see DialogField#getNumberOfControls
	 */
	public abstract int getNumberOfControls();

	/**
	 * Returns the list control. When called the first time, the control will be created.
	 * 
	 * @param parent The parent composite when called the first time, or <code>null</code> after.
	 */
	public abstract Control getListControl(Composite parent);

	/**
	 * Returns the internally used table viewer.
	 */
	public abstract TableViewer getTableViewer();

	/**
	 * Returns the composite containing the buttons. When called the first time, the control will be created.
	 * 
	 * @param parent The parent composite when called the first time, or <code>null</code> after.
	 */
	public abstract Composite getButtonBox(Composite parent);

	/*
	 * @see DialogField#dialogFieldChanged
	 */
	public abstract void dialogFieldChanged();

	/**
	 * Sets a button enabled or disabled.
	 */
	public abstract void enableButton(int index, boolean enable);

	/**
	 * Sets the elements shown in the list.
	 */
	public abstract void setElements(Collection elements);

	/**
	 * Gets the elements shown in the list. The list returned is a copy, so it can be modified by the user.
	 */
	public abstract List getElements();

	/**
	 * Gets the elements shown at the given index.
	 */
	public abstract Object getElement(int index);

	/**
	 * Gets the index of an element in the list or -1 if element is not in list.
	 */
	public abstract int getIndexOfElement(Object elem);

	/**
	 * Replace an element.
	 */
	public abstract void replaceElement(Object oldElement, Object newElement)
			throws IllegalArgumentException;

	/**
	 * Adds an element at the end of the list.
	 */
	public abstract void addElement(Object element);

	/**
	 * Adds an element at a position.
	 */
	public abstract void addElement(Object element, int index);

	/**
	 * Adds elements at the end of the list.
	 */
	public abstract void addElements(List elements);

	/**
	 * Adds an element at a position.
	 */
	public abstract void removeAllElements();

	/**
	 * Removes an element from the list.
	 */
	public abstract void removeElement(Object element)
			throws IllegalArgumentException;

	/**
	 * Removes elements from the list.
	 */
	public abstract void removeElements(List elements);

	/**
	 * Gets the number of elements
	 */
	public abstract int getSize();

	public abstract void selectElements(ISelection selection);

	public abstract void selectFirstElement();

	public abstract void postSetSelection(final ISelection selection);

	/**
	 * Refreshes the table.
	 */
	public abstract void refresh();

	/**
	 * Returns the selected elements.
	 */
	public abstract List getSelectedElements();

}