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
package org.springframework.ide.eclipse.webflow.ui.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidator;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;

/**
 * Some helper methods for {@link WebflowEditor}
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class WebflowUtils {

	private static final Set<IMethod> NO_METHOD_MATCHES = new HashSet<IMethod>();

	public static WebflowEditor getActiveFlowEditor() {

		IEditorPart editorPart = Activator.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editorPart instanceof WebflowEditor) {
			return (WebflowEditor) editorPart;
		}
		return null;
	}

	public static WebflowEditorInput getActiveFlowEditorInput() {

		WebflowEditor editor = getActiveFlowEditor();
		if (editor != null) {
			return (WebflowEditorInput) editor.getEditorInput();
		}
		return null;
	}

	public static IWebflowConfig getActiveWebflowConfig() {
		WebflowEditorInput editorInput = getActiveFlowEditorInput();
		if (editorInput != null && editorInput.getFile() != null) {
			IWebflowProject project = org.springframework.ide.eclipse.webflow.core.Activator
					.getModel().getProject(editorInput.getFile().getProject());
			if (project != null) {
				return project.getConfig(editorInput.getFile());
			}
		}
		return null;
	}

	public static Set<IBean> getBeansFromEditorInput() {
		return WebflowModelUtils.getBeans(getActiveWebflowConfig());
	}

	public static Set<IMethod> getActionMethods(IDOMNode node) {
		Set<IBean> beans = getBeansFromEditorInput();
		String className = null;
		for (IBean bean : beans) {
			if (bean.getElementName().equals(
					BeansEditorUtils.getAttribute(node, "bean"))) {
				className = BeansModelUtils.getBeanClass(bean, null);
			}
		}

		IType type = JdtUtils.getJavaType(getActiveFlowEditorInput().getFile()
				.getProject(), className);
		if (type != null) {
			if ("bean-action".equals(node.getLocalName())) {
				return Introspector.findAllMethods(type, WebflowModelUtils
						.getBeanMethodFilter());
			}
			else {
				return Introspector.findAllMethods(type, WebflowModelUtils
						.getBeanActionMethodFilter());
			}
		}
		return NO_METHOD_MATCHES;
	}

	public static String[] getWebflowConfigNames() {
		IWebflowProject project = getActiveWebflowConfig().getProject();
		return WebflowModelUtils.getWebflowConfigNames(project).toArray(
				new String[0]);
	}

	public static boolean isValid(IWebflowModelElement element) {
		Set<ValidationProblem> validationProblems = validate(element);
		return validationProblems == null || validationProblems.size() == 0;
	}

	public static Set<ValidationProblem> validate(IWebflowModelElement element) {
		IWebflowState webflowState = WebflowModelUtils.getWebflowState(element,
				true);
		if (webflowState != null) {
			IWebflowConfig config = (IWebflowConfig) webflowState
					.getElementParent();
			NoMarkerCreatingWebflowValidator validator = new NoMarkerCreatingWebflowValidator();
			Set<IResource> affectedResources = new HashSet<IResource>();
			affectedResources.add(config.getElementResource());
			try {
				validator
						.validate(affectedResources, new NullProgressMonitor());
				return validator.getValidationProblems();
			}
			catch (CoreException e) {
			}
		}
		return null;
	}

	public static String getErrorTooltip(IWebflowModelElement element) {
		StringBuffer buf = new StringBuffer();
		Set<ValidationProblem> validationProblems = validate(element);
		if (validationProblems != null && validationProblems.size() > 0) {
			buf.append("\n\nProblems:");
			for (ValidationProblem problem : validationProblems) {
				buf.append("\n");
				buf.append(problem.getMessage());
			}
		}

		return buf.toString();
	}

	public static String[] getStateId(IWebflowModelElement parent) {
		IWebflowState webflowState = WebflowModelUtils.getWebflowState(parent,
				false);
		List<String> stateIds = new ArrayList<String>();
		if (webflowState != null) {
			for (IState state : webflowState.getStates()) {
				stateIds.add(state.getId());
			}
		}
		return stateIds.toArray(new String[stateIds.size()]);
	}

	private static class NoMarkerCreatingWebflowValidator extends
			WebflowValidator {

		private Set<ValidationProblem> validationProblems = new HashSet<ValidationProblem>();
		
		public NoMarkerCreatingWebflowValidator() {
			setMarkerId(VALIDATOR_ID);
		}

		protected void createProblemMarker(IResource resource,
				ValidationProblem problem) {
			if (problem.getSeverity() == IValidationProblemMarker.SEVERITY_ERROR) {
				validationProblems.add(problem);
			}
		}

		public void cleanup(IResource resource, IProgressMonitor monitor) {
		}

		public Set<ValidationProblem> getValidationProblems() {
			return this.validationProblems;
		}

		public boolean hasErrors() {
			return this.validationProblems.size() > 0;
		}
		
		@Override
		protected String getValidatorId() {
			return VALIDATOR_ID;
		}
		
	}

}
