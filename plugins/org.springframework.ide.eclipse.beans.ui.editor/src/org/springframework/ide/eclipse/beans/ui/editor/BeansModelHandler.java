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
package org.springframework.ide.eclipse.beans.ui.editor;

import org.eclipse.wst.sse.core.internal.document.IDocumentCharsetDetector;
import org.eclipse.wst.sse.core.internal.document.IDocumentLoader;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.AbstractModelHandler;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IModelHandler;
import org.eclipse.wst.sse.core.internal.provisional.IModelLoader;
import org.eclipse.wst.xml.core.internal.encoding.XMLDocumentCharsetDetector;
import org.eclipse.wst.xml.core.internal.encoding.XMLDocumentLoader;
import org.eclipse.wst.xml.core.internal.modelhandler.XMLModelLoader;

/**
 * Provides model handling for Spring beans config files.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansModelHandler extends AbstractModelHandler implements
		IModelHandler {
	/**
	 * Needs to match what's in plugin registry. In fact, can be overwritten at
	 * run time with what's in registry! (so should never be 'final')
	 */
	private static String modelHandlerID = "org.eclipse.wst.sse.core.handler.beansConfig";

	/**
	 * Needs to match what's in plugin registry. In fact, can be overwritten at
	 * run time with what's in registry! (so should never be 'final')
	 */
	private static String associatedContentTypeID = Activator.PLUGIN_ID
			+ ".beansConfig";

	public BeansModelHandler() {
		setId(modelHandlerID);
		setAssociatedContentTypeId(associatedContentTypeID);
	}

	@Override
	public IDocumentCharsetDetector getEncodingDetector() {
		return new XMLDocumentCharsetDetector();
	}

	public IDocumentLoader getDocumentLoader() {
		return new XMLDocumentLoader();
	}

	public IModelLoader getModelLoader() {
		return new XMLModelLoader();
	}
}
