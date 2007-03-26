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
package org.springframework.ide.eclipse.webflow.ui.editor;

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
 */
@SuppressWarnings("restriction")
public class WebflowModelHandler extends AbstractModelHandler implements IModelHandler {
	
	/**
	 * Needs to match what's in plugin registry. In fact, can be overwritten at
	 * run time with what's in registry! (so should never be 'final')
	 */
	private static String modelHandlerID = "org.eclipse.wst.sse.core.handler.webflowConfig";

	/**
	 * Needs to match what's in plugin registry. In fact, can be overwritten at
	 * run time with what's in registry! (so should never be 'final')
	 */
	private static String associatedContentTypeID = Activator.PLUGIN_ID + ".webflowConfig";

	/**
	 * 
	 */
	public WebflowModelHandler() {
		setId(modelHandlerID);
		setAssociatedContentTypeId(associatedContentTypeID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.sse.core.internal.ltk.modelhandler.AbstractModelHandler#getEncodingDetector()
	 */
	@Override
	public IDocumentCharsetDetector getEncodingDetector() {
		return new XMLDocumentCharsetDetector();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler#getDocumentLoader()
	 */
	public IDocumentLoader getDocumentLoader() {
		return new XMLDocumentLoader();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.wst.sse.core.internal.ltk.modelhandler.IModelHandler#getModelLoader()
	 */
	public IModelLoader getModelLoader() {
		return new XMLModelLoader();
	}
}
