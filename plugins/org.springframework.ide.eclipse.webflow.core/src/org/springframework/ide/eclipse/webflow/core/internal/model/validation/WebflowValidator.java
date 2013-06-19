/*******************************************************************************
 * Copyright (c) 2007, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.validation;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * {@link IValidator} implementation that is responsible for validating the
 * {@link IWebflowModel}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidator extends AbstractValidator {

	public static final String VALIDATOR_ID = Activator.PLUGIN_ID
			+ ".validator";

	public Set<IResource> deriveResources(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof ISpringProject) {
			object = Activator.getModel().getProject(
					((ISpringProject) object).getProject());
		}
		else if (object instanceof IFile) {
			IWebflowProject project = Activator.getModel().getProject(
					((IFile) object).getProject());
			if (project != null) {
				object = project.getConfig((IFile) object);
			}
		}
		if (object instanceof IWebflowModelElement) {
			if (object instanceof IWebflowProject) {
				for (IWebflowConfig wc : ((IWebflowProject) object)
						.getConfigs()) {
					resources.add(wc.getElementResource());
				}
			}
			else if (object instanceof IResourceModelElement) {
				resources.add(((IResourceModelElement) object)
						.getElementResource());
			}
		}
		return resources;
	}

	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (WebflowModelUtils.isWebflowConfig(resource)) {
			resources.add(resource);
		}
		else if (JdtUtils.isClassPathFile(resource)) {
			IWebflowProject webflowProject = Activator.getModel().getProject(
					resource.getProject());
			if (webflowProject != null) {
				for (IWebflowConfig config : webflowProject.getConfigs()) {
					resources.add(config.getElementResource());
				}
			}

		}
		else if (BeansCoreUtils.isBeansConfig(resource)) {
			IBeansConfig bc = BeansCorePlugin.getModel().getConfig(
			        BeansConfigFactory.getConfigId((IFile) resource));
			IWebflowProject wp = Activator.getModel().getProject(
					resource.getProject());
			for (IWebflowConfig fc : wp.getConfigs()) {
				if (fc.getBeansConfigs().contains(bc)) {
					resources.add(fc.getElementResource());
				}

				for (IModelElement me : fc.getBeansConfigs()) {
					if (me instanceof IBeansConfigSet
							&& ((IBeansConfigSet) me).getConfigs().contains(bc)) {
						resources.add(fc.getElementResource());
					}
				}
			}
		}
		return resources;
	}

	@Override
	protected IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		if (rootElement instanceof IWebflowState) {
			IWebflowState state = (IWebflowState) rootElement;
			IWebflowConfig config = WebflowModelUtils
					.getWebflowConfig((IFile) state.getElementResource());
			return new WebflowValidationContext(state, config);
		}
		return null;
	}

	@Override
	protected boolean supports(IModelElement element) {
		return (element instanceof IWebflowModelElement);
	}

	@Override
	protected IValidationElementLifecycleManager createValidationElementLifecycleManager() {
		return new WebflowStateLifecycleManager();
	}

	private static class WebflowStateLifecycleManager implements
			IValidationElementLifecycleManager {

		private IStructuredModel model = null;

		private IFile file = null;

		private IWebflowState rootElement;

		public void destroy() {
			if (model != null) {
				model.releaseFromRead();
			}
		}

		public Set<IResourceModelElement> getContextElements() {
			Set<IResourceModelElement> contextElements = new HashSet<IResourceModelElement>();
			contextElements.add(rootElement);
			return contextElements;
		}

		public IResourceModelElement getRootElement() {
			return this.rootElement;
		}

		public void init(IResource resource) {
			if (resource instanceof IFile) {
				this.file = (IFile) resource;
			}
			try {
				model = StructuredModelManager.getModelManager()
						.getExistingModelForRead(resource);
				if (model == null) {
					model = StructuredModelManager.getModelManager()
							.getModelForRead(file);

				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model)
							.getDocument();
					rootElement = new WebflowState(WebflowModelUtils
							.getWebflowConfig(file));
					rootElement.init((IDOMNode) document.getDocumentElement(),
							null);
				}
			}
			catch (Exception e) {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
	}
}
