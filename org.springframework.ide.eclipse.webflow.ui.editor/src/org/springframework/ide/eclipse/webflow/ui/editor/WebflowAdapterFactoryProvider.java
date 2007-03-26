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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler;
import org.eclipse.wst.sse.core.internal.model.FactoryRegistry;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.CMDocumentManager;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.ui.internal.DOMObserver;
import org.eclipse.wst.xml.ui.internal.XMLUIPlugin;
import org.eclipse.wst.xml.ui.internal.preferences.XMLUIPreferenceNames;
import org.eclipse.wst.xml.ui.internal.registry.AdapterFactoryProviderForXML;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansJFaceNodeAdapterFactory;
import org.springframework.util.Assert;

/**
 * 
 */
@SuppressWarnings("restriction")
public class WebflowAdapterFactoryProvider extends AdapterFactoryProviderForXML {

	/**
	 * 
	 * 
	 * @param structuredModel 
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected void addContentBasedFactories(IStructuredModel structuredModel) {
		FactoryRegistry factoryRegistry = structuredModel.getFactoryRegistry();
		Assert.notNull(factoryRegistry, "No factory registered");
		INodeAdapterFactory factory = factoryRegistry.getFactoryFor(IJFaceNodeAdapter.class);
		if (factory == null) {
			factory = new BeansJFaceNodeAdapterFactory();
			factoryRegistry.addFactory(factory);
		}

		// Stuff from super method (inferred grammar support)
		if (structuredModel != null) {
			ModelQuery modelQuery = ModelQueryUtil.getModelQuery(structuredModel);
			if (modelQuery != null) {
				CMDocumentManager documentManager = modelQuery.getCMDocumentManager();
				if (documentManager != null) {
					IPreferenceStore store = XMLUIPlugin.getDefault().getPreferenceStore();
					boolean useInferredGrammar = (store != null) ? store
							.getBoolean(XMLUIPreferenceNames.USE_INFERRED_GRAMMAR) : true;

					documentManager.setPropertyEnabled(CMDocumentManager.PROPERTY_ASYNC_LOAD, true);
					documentManager.setPropertyEnabled(CMDocumentManager.PROPERTY_AUTO_LOAD, false);
					documentManager.setPropertyEnabled(CMDocumentManager.PROPERTY_USE_CACHED_RESOLVED_URI, true);
					DOMObserver domObserver = new DOMObserver(structuredModel);
					domObserver.setGrammarInferenceEnabled(useInferredGrammar);
					domObserver.init();
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * @param contentTypeDescription 
	 * 
	 * @return 
	 */
	@Override
	public boolean isFor(IDocumentTypeHandler contentTypeDescription) {
		return (contentTypeDescription instanceof WebflowModelHandler);
	}
}
