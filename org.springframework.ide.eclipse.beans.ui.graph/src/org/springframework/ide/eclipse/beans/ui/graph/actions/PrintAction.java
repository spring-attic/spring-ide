/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.print.PrintGraphicalViewerOperation;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.IEditorPart;

/**
 * This is a copy of v2.1.2 of
 * <code>org.eclipse.gef.ui.actions.PrintAction</code>. It was necessary because
 * v3.0 extends from <code>WorkbenchEditorPart</code> which is not compatible
 * anymore.
 */
public class PrintAction extends EditorPartAction {
/**
 * Constructor for PrintAction.
 * @param editor The EditorPart associated with this PrintAction
 */
public PrintAction(IEditorPart editor) {
	super(editor);
}

/**
 * @see org.eclipse.gef.ui.actions.EditorPartAction#calculateEnabled()
 */
protected boolean calculateEnabled() {
	PrinterData[] printers = Printer.getPrinterList();
	return printers != null && printers.length > 0;
}

/**
 * @see org.eclipse.gef.ui.actions.EditorPartAction#init()
 */
protected void init() {
	setId(GEFActionConstants.PRINT);
}

/**
 * @see org.eclipse.jface.action.Action#run()
 */
public void run() {
	GraphicalViewer viewer;
	viewer = (GraphicalViewer)getEditorPart().getAdapter(GraphicalViewer.class);
	
	PrintDialog dialog = new PrintDialog(viewer.getControl().getShell(), SWT.NULL);
	PrinterData data = dialog.open();
	
	if (data != null) {
		PrintGraphicalViewerOperation op = 
					new PrintGraphicalViewerOperation(new Printer(data), viewer);
		op.run(getEditorPart().getTitle());
	}	
}
}
