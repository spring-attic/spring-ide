/*******************************************************************************
 * Copyright (c) 2008, 2013 Spring IDE Developers
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.model.builder.IDocumentFactory;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;

/**
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.3.1
 */
@SuppressWarnings("restriction")
class AspectDefinitionBuilderHelper {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String ASPECT_DEFINITION_BUILDER_ELEMENT = "aspectDefinitionBuilder";

	private static final String ASPECT_DEFINITION_BUILDER_EXTENSION_POINT = Activator.PLUGIN_ID
			+ ".aspectdefinitionbuilder";

	private Set<IAspectDefinitionBuilder> builders = null;

	private DefaultDocumentFactory documentFactory = null;
	
	public AspectDefinitionBuilderHelper() {
		builders = loadAspectDefinitionBuilder();
		documentFactory = new DefaultDocumentFactory();
	}

	private Set<IAspectDefinitionBuilder> loadAspectDefinitionBuilder() {
		Set<IAspectDefinitionBuilder> builders = new HashSet<IAspectDefinitionBuilder>();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				ASPECT_DEFINITION_BUILDER_EXTENSION_POINT);
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (ASPECT_DEFINITION_BUILDER_ELEMENT.equals(config.getName())
							&& config.getAttribute(CLASS_ATTRIBUTE) != null) {
						try {
							Object handler = config.createExecutableExtension(CLASS_ATTRIBUTE);
							if (handler instanceof IAspectDefinitionBuilder) {
								builders.add((IAspectDefinitionBuilder) handler);
							}
						}
						catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
		return builders;
	}

	public List<IAspectDefinition> buildAspectDefinitions(IFile file, IProjectClassLoaderSupport classLoaderSupport) {
		List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();

		for (IAspectDefinitionBuilder builder : builders) {
			builder.buildAspectDefinitions(aspectInfos, file, classLoaderSupport, documentFactory);
		}

		return aspectInfos;
	}

	private class DefaultDocumentFactory implements IDocumentFactory {

		private Map<IFile, IDOMModel> cache = new ConcurrentHashMap<IFile, IDOMModel>();

		@SuppressWarnings("deprecation")
		public IDOMDocument createDocument(IFile file) {
			if (cache.containsKey(file)) {
				return cache.get(file).getDocument();
			}

			IStructuredModel model = null;
			try {
				if (file instanceof ExternalFile) {
					try {
						model = StructuredModelManager.getModelManager().getModelForRead(file.getName(),
								file.getContents(), null);
					}
					catch (RuntimeException e) {
						// WTP throws an exception is this is not an XML document
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
					try {
						IDOMDocument document = ((IDOMModel) model).getDocument();
						if (document != null && document.getDocumentElement() != null) {
							cache.put(file, (IDOMModel) model);
							return document;
						}
					} catch (RuntimeException e) {
						if (model != null) {
							model.releaseFromRead();
						}
						return null;
					}
				}
			}
			catch (IOException e) {
				Activator.log(e);
			}
			catch (CoreException e) {
				Activator.log(e);
			}
			return null;
		}
		
		protected void releaseModels() {
			for (IDOMModel document : cache.values()) {
				try {
					document.releaseFromRead();
				}
				catch (Exception e) {
					// ignore
				}
			}
		}
	}

	public void close() {
		documentFactory.releaseModels();
	}

}
