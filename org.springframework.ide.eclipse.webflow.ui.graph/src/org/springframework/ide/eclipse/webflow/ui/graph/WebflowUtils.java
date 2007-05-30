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
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationVisitor;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidator;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.core.util.BeanActionMethodSearchRequestor;
import org.springframework.ide.eclipse.webflow.core.util.BeanMethodSearchRequestor;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;

/**
 * Some helper methods for {@link WebflowEditor}
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class WebflowUtils {

	private static final List<IMethod> NO_METHOD_MATCHES = new ArrayList<IMethod>();

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

	public static List<IMethod> getActionMethods(IDOMNode node) {
		Set<IBean> beans = getBeansFromEditorInput();
		String className = null;
		for (IBean bean : beans) {
			if (bean.getElementName().equals(
					BeansEditorUtils.getAttribute(node, "bean"))) {
				className = BeansModelUtils.getBeanClass(bean, null);
			}
		}

		IType type = BeansModelUtils.getJavaType(getActiveFlowEditorInput()
				.getFile().getProject(), className);
		if (type != null) {
			if ("bean-action".equals(node.getLocalName())) {
				MethodSearchRequestor requestor = new MethodSearchRequestor(
						null);
				try {
					IMethod[] methods = type.getMethods();
					if (methods != null) {
						for (IMethod method : methods) {
							requestor.acceptSearchMatch(method, "");
						}
					}
				}
				catch (JavaModelException e) {
				}
				catch (CoreException e) {
				}
				return requestor.getMethods();
			}
			else {
				ActionMethodSearchRequestor requestor = new ActionMethodSearchRequestor(
						null);
				try {
					IMethod[] methods = type.getMethods();
					if (methods != null) {
						for (IMethod method : methods) {
							requestor.acceptSearchMatch(method, "");
						}
					}
				}
				catch (JavaModelException e) {
				}
				catch (CoreException e) {
				}
				return requestor.getMethods();
			}
		}
		return NO_METHOD_MATCHES;
	}

	private static class ActionMethodSearchRequestor extends
			BeanActionMethodSearchRequestor {

		private List<IMethod> methods = new ArrayList<IMethod>();

		public ActionMethodSearchRequestor(ContentAssistRequest request) {
			super(request);
		}

		protected void createMethodProposal(IMethod method, int relevance) {
			methods.add(method);
		}

		public List<IMethod> getMethods() {
			return methods;
		}
	}

	private static class MethodSearchRequestor extends
			BeanMethodSearchRequestor {

		private List<IMethod> methods = new ArrayList<IMethod>();

		public MethodSearchRequestor(ContentAssistRequest request) {
			super(request);
		}

		protected void createMethodProposal(IMethod method, int relevance) {
			methods.add(method);
		}

		public List<IMethod> getMethods() {
			return methods;
		}
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

	public static Set<ValidationProblem> validate(
			IWebflowModelElement element) {
		IWebflowState webflowState = WebflowModelUtils.getWebflowState(element,
				true);
		if (webflowState != null) {
			IWebflowConfig config = (IWebflowConfig) webflowState
					.getElementParent();
			WebflowValidationContext validationContext = new WebflowValidationContext(
					config.getElementResource(), webflowState, config);
			WebflowValidationVisitor validationVisitor = new WebflowValidationVisitor(
					validationContext, ValidationRuleDefinitionFactory
							.getEnabledRuleDefinitions(
									WebflowValidator.VALIDATOR_ID, config
											.getElementResource().getProject()));
			webflowState.accept(validationVisitor, new NullProgressMonitor());
			return validationContext.getProblems();
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

}
