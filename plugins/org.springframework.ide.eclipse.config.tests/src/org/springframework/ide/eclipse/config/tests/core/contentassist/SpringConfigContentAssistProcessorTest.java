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
package org.springframework.ide.eclipse.config.tests.core.contentassist;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDocumentCache;
import org.eclipse.wst.xml.core.internal.contentmodel.util.CMDocumentCacheListener;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.ProjectClasspathExtensibleUriResolver;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.schemas.AopSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntFileSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntJmsSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntStreamSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.IntegrationSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.TxSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.WebFlowConfigSchemaConstants;
import org.springframework.ide.eclipse.config.tests.AbstractConfigTestCase;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.overview.OverviewFormPage;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class SpringConfigContentAssistProcessorTest extends AbstractConfigTestCase {

	public void testAopFileForBeansChildren() throws Exception {
		cEditor = openFileInEditor("src/aop-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		IDOMElement node = (IDOMElement) root.getData();
		SpringConfigContentAssistProcessor xmlProcessor = page.getXmlProcessor();
		List<String> children = xmlProcessor.getChildNames(node);

		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				BeansSchemaConstants.URI) + ":" + BeansSchemaConstants.ELEM_ALIAS));
		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				BeansSchemaConstants.URI) + ":" + BeansSchemaConstants.ELEM_BEAN));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				BeansSchemaConstants.URI) + ":" + BeansSchemaConstants.ELEM_BEANS));

		assertTrue(children.contains(AopSchemaConstants.ELEM_CONFIG));
		assertFalse(children.contains(AopSchemaConstants.ELEM_POINTCUT));

		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				TxSchemaConstants.URI) + ":" + TxSchemaConstants.ELEM_ADVICE));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				TxSchemaConstants.URI) + ":" + TxSchemaConstants.ELEM_ATTRIBUTES));

		assertFalse(children.contains(WebFlowConfigSchemaConstants.ELEM_FLOW_BUILDER_SERVICES));
	}

	public void testBeansFileForBeansChildren() throws Exception {
		cEditor = openFileInEditor("src/beans-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		IDOMElement node = (IDOMElement) root.getData();
		SpringConfigContentAssistProcessor xmlProcessor = page.getXmlProcessor();
		List<String> children = xmlProcessor.getChildNames(node);

		assertTrue(children.contains(BeansSchemaConstants.ELEM_ALIAS));
		assertTrue(children.contains(BeansSchemaConstants.ELEM_BEAN));
		assertFalse(children.contains(BeansSchemaConstants.ELEM_BEANS));

		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				AopSchemaConstants.URI) + ":" + AopSchemaConstants.ELEM_CONFIG));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				AopSchemaConstants.URI) + ":" + AopSchemaConstants.ELEM_POINTCUT));

		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				TxSchemaConstants.URI) + ":" + TxSchemaConstants.ELEM_ADVICE));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				TxSchemaConstants.URI) + ":" + TxSchemaConstants.ELEM_ATTRIBUTES));

		assertFalse(children.contains(WebFlowConfigSchemaConstants.ELEM_FLOW_BUILDER_SERVICES));
	}

	public void testEmptyFileForBeansChildren() throws Exception {
		cEditor = openFileInEditor("src/empty-beans.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		IDOMElement node = (IDOMElement) root.getData();
		SpringConfigContentAssistProcessor xmlProcessor = page.getXmlProcessor();
		List<String> children = xmlProcessor.getChildNames(node);

		assertTrue(children.contains(BeansSchemaConstants.ELEM_ALIAS));
		assertTrue(children.contains(BeansSchemaConstants.ELEM_BEAN));
		assertFalse(children.contains(BeansSchemaConstants.ELEM_BEANS));

		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				AopSchemaConstants.URI) + ":" + AopSchemaConstants.ELEM_CONFIG));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				AopSchemaConstants.URI) + ":" + AopSchemaConstants.ELEM_POINTCUT));

		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				TxSchemaConstants.URI) + ":" + TxSchemaConstants.ELEM_ADVICE));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				TxSchemaConstants.URI) + ":" + TxSchemaConstants.ELEM_ATTRIBUTES));

		assertFalse(children.contains(WebFlowConfigSchemaConstants.ELEM_FLOW_BUILDER_SERVICES));
	}

	public void testIntegrationFileForBeansChildren() throws Exception {
		cEditor = openFileInEditor("src/integration-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		IDOMDocument document = cEditor.getDomDocument();
		CMDocumentCache cache = ModelQueryUtil.getCMDocumentCache(document);
		final String siUri = ConfigCoreUtils.getSelectedSchemaLocation(document, IntegrationSchemaConstants.URI);
		final String siJmsUri = ConfigCoreUtils.getSelectedSchemaLocation(document, IntJmsSchemaConstants.URI);
		final String siStreamUri = ConfigCoreUtils.getSelectedSchemaLocation(document, IntStreamSchemaConstants.URI);
		final CountDownLatch latch = new CountDownLatch(3);
		if (cache.getStatus(siUri) == CMDocumentCache.STATUS_LOADED) {
			latch.countDown();
		}
		if (cache.getStatus(siJmsUri) == CMDocumentCache.STATUS_LOADED) {
			latch.countDown();
		}
		if (cache.getStatus(siStreamUri) == CMDocumentCache.STATUS_LOADED) {
			latch.countDown();
		}

		cache.addListener(new CMDocumentCacheListener() {

			public void cacheUpdated(CMDocumentCache cache, String uri, int oldStatus, int newStatus,
					CMDocument cmDocument) {
				if (newStatus == CMDocumentCache.STATUS_LOADED && oldStatus != CMDocumentCache.STATUS_LOADED) {
					if (uri.equals(siUri) || uri.equals(siJmsUri) || uri.equals(siStreamUri)) {
						latch.countDown();
					}
				}

			}

			public void cacheCleared(CMDocumentCache cache) {
				// TODO Auto-generated method stub

			}

		});
		assertTrue("Document initialization did not complete before timeout.", latch.await(30, TimeUnit.SECONDS));

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		IDOMElement node = (IDOMElement) root.getData();
		SpringConfigContentAssistProcessor xmlProcessor = page.getXmlProcessor();
		List<String> children = xmlProcessor.getChildNames(node);

		assertTrue(children.contains(IntegrationSchemaConstants.ELEM_AGGREGATOR));
		assertTrue(children.contains(IntegrationSchemaConstants.ELEM_CHANNEL));
		assertFalse(children.contains(IntegrationSchemaConstants.ELEM_WIRE_TAP));

		assertFalse(children.contains(IntFileSchemaConstants.ELEM_FILE_TO_BYTES_TRANSFORMER));

		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				IntJmsSchemaConstants.URI) + ":" + IntJmsSchemaConstants.ELEM_CHANNEL));
		assertFalse(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				IntJmsSchemaConstants.URI) + ":" + IntJmsSchemaConstants.ELEM_REPLY_TO));

		assertTrue(children.contains(ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(),
				IntStreamSchemaConstants.URI) + ":" + IntStreamSchemaConstants.ELEM_STDIN_CHANNEL_ADAPTER));
	}

	public void testScopedFileForBeansChildren() throws Exception {
		cEditor = openFileInEditor("src/scoped-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		AbstractConfigFormPage page = cEditor.getFormPage(OverviewFormPage.ID);
		Thread.sleep(StsTestUtil.WAIT_TIME);
		cEditor.setActivePage(page.getId());
		assertNotNull("Could not load overview page.", page.getMasterPart());

		IDOMDocument document = cEditor.getDomDocument();
		CMDocumentCache cache = ModelQueryUtil.getCMDocumentCache(document);

		final String batchUri = ConfigCoreUtils.getSelectedSchemaLocation(document, BatchSchemaConstants.URI);
		IFile file = cEditor.getResourceFile();

		ProjectClasspathExtensibleUriResolver resolver = new ProjectClasspathExtensibleUriResolver();
		final String resolvedBatchUri = resolver.resolve(file, null, batchUri, batchUri);

		final CountDownLatch latch = new CountDownLatch(1);
		if (cache.getStatus(resolvedBatchUri) == CMDocumentCache.STATUS_LOADED) {
			latch.countDown();
		}
		cache.addListener(new CMDocumentCacheListener() {
			public void cacheUpdated(CMDocumentCache cache, String uri, int oldStatus, int newStatus,
					CMDocument cmDocument) {
				if (newStatus == CMDocumentCache.STATUS_LOADED && oldStatus != CMDocumentCache.STATUS_LOADED) {
					if (uri.equals(resolvedBatchUri)) {
						latch.countDown();
					}
				}
			}

			public void cacheCleared(CMDocumentCache cache) {
			}

		});

		assertTrue("Document initialization did not complete before timeout.", latch.await(30, TimeUnit.SECONDS));

		TreeViewer treeViewer = (TreeViewer) page.getMasterPart().getViewer();
		TreeItem root = treeViewer.getTree().getItem(0);
		IDOMElement node = (IDOMElement) root.getData();
		SpringConfigContentAssistProcessor xmlProcessor = page.getXmlProcessor();
		List<String> children = xmlProcessor.getChildNames(node);

		assertTrue(children.contains(BeansSchemaConstants.ELEM_ALIAS));
		assertTrue(children.contains(BeansSchemaConstants.ELEM_BEAN));
		assertFalse(children.contains(BeansSchemaConstants.ELEM_BEANS));

		assertFalse(children.contains(BatchSchemaConstants.ELEM_JOB));

		TreeItem jobItem = root.getItem(1);
		IDOMElement jobNode = (IDOMElement) jobItem.getData();
		children = xmlProcessor.getChildNames(jobNode);

		assertFalse(children.contains(BatchSchemaConstants.ELEM_JOB));
		assertTrue(children.contains(BatchSchemaConstants.ELEM_STEP));
		assertTrue(children.contains(BatchSchemaConstants.ELEM_SPLIT));
	}

}
