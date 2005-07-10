package org.springframework.ide.eclipse.beans.ui.editor;

import org.eclipse.jface.util.Assert;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.wst.sse.core.internal.ltk.modelhandler.IDocumentTypeHandler;
import org.eclipse.wst.sse.core.internal.model.FactoryRegistry;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.properties.XMLPropertySourceAdapterFactory;
import org.eclipse.wst.xml.ui.internal.registry.AdapterFactoryProviderForXML;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansJFaceNodeAdapterFactory;

public class BeansAdapterFactoryProvider extends AdapterFactoryProviderForXML {

	protected void addContentBasedFactories(IStructuredModel structuredModel) {
		FactoryRegistry factoryRegistry = structuredModel.getFactoryRegistry();
		Assert.isNotNull(factoryRegistry, "No factory registered");
		INodeAdapterFactory factory = factoryRegistry.getFactoryFor(
														IPropertySource.class);
		if (factory == null) {
			factory = new XMLPropertySourceAdapterFactory();
			factoryRegistry.addFactory(factory);
		}
		factory = factoryRegistry.getFactoryFor(IJFaceNodeAdapter.class);
		if (factory == null) {
			factory = new BeansJFaceNodeAdapterFactory();
			factoryRegistry.addFactory(factory);
		}
		super.addContentBasedFactories(structuredModel);
	}

	public boolean isFor(IDocumentTypeHandler contentTypeDescription) {
		return (contentTypeDescription instanceof BeansModelHandler);
	}
}
