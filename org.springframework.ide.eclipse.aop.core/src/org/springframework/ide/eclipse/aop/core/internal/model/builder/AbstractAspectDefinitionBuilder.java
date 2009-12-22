/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * Utility class that encapsulates the loading of a {@link IDOMDocument} from the given
 * {@link IFile}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractAspectDefinitionBuilder implements IAspectDefinitionBuilder {

	public final List<IAspectDefinition> buildAspectDefinitions(IFile file,
			IProjectClassLoaderSupport classLoaderSupport) {
		final List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();

		IStructuredModel model = null;
		try {
			if (file instanceof ExternalFile) {
				if (model == null) {
					model = StructuredModelManager.getModelManager().getModelForRead(
							file.getName(), file.getContents(), null);
				}
			}
			else {
				try {
					model = StructuredModelManager.getModelManager().getExistingModelForRead(file);
				}
				catch (RuntimeException e) {
					// sometimes WTP throws a NPE in concurrency situations
				}
				if (model == null) {
					model = StructuredModelManager.getModelManager().getModelForRead(file);
				}

			}
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				if (document != null && document.getDocumentElement() != null) {
					doBuildAspectDefinitions(document, file, aspectInfos, classLoaderSupport);
				}
			}
		}
		catch (IOException e) {
			Activator.log(e);
		}
		catch (CoreException e) {
			Activator.log(e);
		}
		finally {
			if (model != null) {
				try {
					model.releaseFromRead();
				}
				catch (Exception e) {
					// sometimes WTP throws a NPE in concurrency situations
				}
			}
		}
		return aspectInfos;
	}

	protected String getAttribute(Node node, String attributeName) {
		if (hasAttribute(node, attributeName)) {
			String value = node.getAttributes().getNamedItem(attributeName).getNodeValue();
			value = StringUtils.replace(value, "\n", " ");
			value = StringUtils.replace(value, "\t", " ");
			return StringUtils.replace(value, "\r", " ");
		}
		return null;
	}

	protected boolean hasAttribute(Node node, String attributeName) {
		return (node != null && node.hasAttributes() && node.getAttributes().getNamedItem(
				attributeName) != null);
	}

	protected abstract void doBuildAspectDefinitions(IDOMDocument document, IFile file,
			List<IAspectDefinition> aspectInfos, IProjectClassLoaderSupport classLoaderSupport);
}
