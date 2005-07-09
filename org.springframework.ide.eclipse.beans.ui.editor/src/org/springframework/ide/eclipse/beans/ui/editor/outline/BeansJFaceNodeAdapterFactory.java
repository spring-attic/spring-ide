package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.ssemodelquery.ModelQueryAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapterFactory;

/**
 * An adapter factory to create JFaceNodeAdapters. Use this adapter factory
 * with a JFaceAdapterContentProvider to display DOM nodes in a tree.
 */
public class BeansJFaceNodeAdapterFactory extends JFaceNodeAdapterFactory {

	protected BeansJFaceNodeAdapter singletonAdapter;

	public BeansJFaceNodeAdapterFactory() {
		this(BeansJFaceNodeAdapterFactory.class, true);
	}

	public BeansJFaceNodeAdapterFactory(Object adapterKey,
										boolean registerAdapters) {
		super(adapterKey, registerAdapters);
	}

	/**
	 * Create a new JFace adapter for the DOM node passed in
	 */
	protected INodeAdapter createAdapter(INodeNotifier node) {
		if (singletonAdapter == null) {

			// create the JFaceNodeAdapter
			singletonAdapter = new BeansJFaceNodeAdapter(this);
			initAdapter(singletonAdapter, node);
		}
		return singletonAdapter;
	}

	protected void initAdapter(INodeAdapter adapter, INodeNotifier node) {

		// register for CMDocumentManager events
		if (((BeansJFaceNodeAdapter)
							 adapter).getCMDocumentManagerListener() != null) {
			ModelQueryAdapter mqadapter = (ModelQueryAdapter)
								   node.getAdapterFor(ModelQueryAdapter.class);
			if (mqadapter != null) {
				ModelQuery mquery = mqadapter.getModelQuery();
				if (mquery != null && mquery.getCMDocumentManager() != null) {
					cmDocumentManager = mquery.getCMDocumentManager();
					cmDocumentManager.addListener(((BeansJFaceNodeAdapter)
									  adapter).getCMDocumentManagerListener());
				}
			}
		}
	}

	public void release() {

		// deregister from CMDocumentManager events
		if (cmDocumentManager != null && singletonAdapter != null &&
					 singletonAdapter.getCMDocumentManagerListener() != null) {
			cmDocumentManager.removeListener(
							  singletonAdapter.getCMDocumentManagerListener());
		}
	}


	public INodeAdapterFactory copy() {
		return new BeansJFaceNodeAdapterFactory(adapterKey,
												shouldRegisterAdapter);
	}
}
