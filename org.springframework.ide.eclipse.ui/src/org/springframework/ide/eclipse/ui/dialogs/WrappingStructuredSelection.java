/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * This {@link IStructuredSelection} implementation fixes WTP problem in class
 * <code>StructuredTextSelection</code> that prevents from reentrant calls to
 * {@link StructuredSelection#getFirstElement()}.
 * <p>
 * Subsequent calls on the WTP class return <code>null</code>.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WrappingStructuredSelection implements IStructuredSelection {

	private final IStructuredSelection wrapped;

	private Object selectedObject;

	public WrappingStructuredSelection(IStructuredSelection wrapped) {
		this.wrapped = wrapped;
	}

	public Object getFirstElement() {
		if (this.selectedObject == null) {
			this.selectedObject = wrapped.getFirstElement();
		}
		return this.selectedObject;
	}

	public boolean isEmpty() {
		return getFirstElement() == null;
	}

	public Iterator iterator() {
		return toList().iterator();
	}

	public int size() {
		return toList().size();
	}

	public Object[] toArray() {
		return toList().toArray();
	}

	public List<Object> toList() {
		List<Object> list = new ArrayList<Object>();
		list.add(getFirstElement());
		return list;
	}

}
