/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views;

import java.util.Iterator;

import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;

/**
 * Adapter for DND support in beans view.
 * 
 * @author Pierre-Antoine Grégoire
 */
public class BeansViewDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {

	private Viewer viewer;

	public BeansViewDragAdapter(Viewer viewer) {
		this.viewer = viewer;
	}

	public Transfer getTransfer() {
        return LocalSelectionTransfer.getInstance();
    }

	public void dragStart(DragSourceEvent event) {
		DragSource dragSource = (DragSource) event.widget;
		Control control = dragSource.getControl();
		if (control != control.getDisplay().getFocusControl()) {
			event.doit = false;
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) viewer
				.getSelection();
		if (selection.isEmpty()) {
			event.doit = false;
			return;
		}
		Object previous = null;
		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object next = it.next();
			if (previous != null
					&& !(previous.getClass().equals(next.getClass()))) {
				// different kinds of Nodes should not be dragged together.
				// N.B.: for now it's not necessary, because only one can be
				// selected, but it's always good to check.
				event.doit = false;
				return;
			} else {
				if (!(next instanceof ConfigNode)) {
					event.doit = false;
					return;
				}
				previous = next;
			}
		}
		LocalSelectionTransfer.getInstance().setSelection(selection);
		LocalSelectionTransfer.getInstance().setSelectionSetTime(event.time & 0xFFFFFFFFL);
		event.doit = true;
	}

	public void dragSetData(DragSourceEvent event) {
        // For consistency set the data to the selection even though
        // the selection is provided by the LocalSelectionTransfer
        // to the drop target adapter.
        event.data = LocalSelectionTransfer.getInstance().getSelection();
    }

	public void dragFinished(DragSourceEvent event) {
        LocalSelectionTransfer.getInstance().setSelection(null);
        LocalSelectionTransfer.getInstance().setSelectionSetTime(0);
    }   
}
