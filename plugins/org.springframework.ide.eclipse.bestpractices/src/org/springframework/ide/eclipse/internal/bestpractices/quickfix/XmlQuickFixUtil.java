/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.springframework.ide.eclipse.bestpractices.BestPracticesPluginConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class XmlQuickFixUtil {

	private static final String LINE_NUMBER_MARKER_ATTRIBUTE_KEY = "lineNumber";

	public static IEditorPart getMarkedEditor(IMarker marker) {

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		return page.findEditor(new FileEditorInput((IFile) marker.getResource()));
	}

	/**
	 * Returns the XML element corresponding to the location of the given
	 * marker. Precondition: The marked element to be returned is a bean element
	 */
	public static Element getMarkerElement(IStructuredModel model, IMarker marker) throws CoreException {
		Element markerElement = null;

		Integer markerLineNumber = (Integer) marker.getAttribute(LINE_NUMBER_MARKER_ATTRIBUTE_KEY);

		int offset = 0;
		try {
			offset = model.getStructuredDocument().getLineOffset(markerLineNumber.intValue());
		}
		catch (BadLocationException e) {
			throw new CoreException(new Status(Status.ERROR, BestPracticesPluginConstants.PLUGIN_ID,
					"Could not read marker offset in document", e));
		}

		IndexedRegion markedXmlRegion = model.getIndexedRegion(offset);
		Node markedXmlNode = (Node) markedXmlRegion;

		if (markedXmlNode.getParentNode() != null
				&& "bean".equalsIgnoreCase(markedXmlNode.getParentNode().getNodeName())) {
			markerElement = (Element) markedXmlNode.getParentNode();
		}
		else if (markedXmlNode.getPreviousSibling() != null
				&& "bean".equalsIgnoreCase(markedXmlNode.getPreviousSibling().getNodeName())) {
			markerElement = (Element) markedXmlNode.getPreviousSibling();
		}
		else if (markedXmlNode.getParentNode() != null && markedXmlNode.getParentNode().getParentNode() != null
				&& "bean".equalsIgnoreCase(markedXmlNode.getParentNode().getParentNode().getNodeName())) {
			markerElement = (Element) markedXmlNode.getParentNode().getParentNode();
		}
		else {
			throw new CoreException(new Status(Status.ERROR, BestPracticesPluginConstants.PLUGIN_ID,
					"Could not find XML Element corresponding to marker"));
		}

		return markerElement;
	}

	/**
	 * Returns an instance of the model manager. You *must* call
	 * {@link IStructuredModel#releaseFromEdit()} on the instance when you are
	 * done with it.
	 */
	public static IStructuredModel getModel(IMarker marker) throws CoreException {
		IModelManager modelManager = StructuredModelManager.getModelManager();

		try {
			return modelManager.getModelForEdit((IFile) marker.getResource());
		}
		catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, BestPracticesPluginConstants.PLUGIN_ID,
					"Could not create model for resource", e));
		}
	}

	public static void saveMarkedFile(IMarker marker) {
		IEditorPart editor = getMarkedEditor(marker);
		if (editor != null) {
			editor.doSave(new NullProgressMonitor());
		}
	}
}
