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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.model.IInlineFlowState;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.core.util.BeanActionMethodSearchRequestor;
import org.springframework.ide.eclipse.webflow.core.util.BeanMethodSearchRequestor;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowModelUtils {

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

	private static final List<IMethod> NO_METHOD_MATCHES = new ArrayList<IMethod>();

	public static final String PROBLEM_MARKER = Activator.PLUGIN_ID
			+ ".problemmarker";

	public static void createMarkerFromProblemReporter(
			Set<ValidationProblem> validationProblems, IFile file) {
		if (validationProblems != null && validationProblems.size() > 0) {
			for (ValidationProblem problem : validationProblems) {
				createProblemMarker(file, problem.getMessage(),
						problem.getSeverity(), problem.getLine());
			}
		}
	}

	public static void createProblemMarker(IResource resource, String message,
			int severity, int line) {
		if (resource != null && resource.isAccessible()) {
			try {

				// First check if specified marker already exists
				IMarker[] markers = resource.findMarkers(PROBLEM_MARKER, false,
						IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					if (l == line) {
						String msg = marker.getAttribute(IMarker.MESSAGE, "");
						if (msg.equals(message)) {
							return;
						}
					}
				}

				// Create new marker
				IMarker marker = resource.createMarker(PROBLEM_MARKER);
				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put(IMarker.MESSAGE, message);
				attributes.put(IMarker.SEVERITY, new Integer(severity));
				if (line > 0) {
					attributes.put(IMarker.LINE_NUMBER, new Integer(line));
				}
				marker.setAttributes(attributes);
			}
			catch (CoreException e) {
				Activator.log(e);
			}
		}
	}

	public static void deleteProblemMarkers(IResource resource) {
		MarkerUtils.deleteMarkers(resource, PROBLEM_MARKER);
	}

	public static IType getActionType(IWebflowConfig config, IDOMNode node) {
		Set<IBean> beans = getBeans(config);
		String className = null;
		for (IBean bean : beans) {
			if (bean.getElementName().equals(
					BeansEditorUtils.getAttribute(node, "bean"))) {
				className = BeansModelUtils.getBeanClass(bean, null);
				break;
			}
		}

		return JdtUtils.getJavaType(config.getProject().getProject(),
				className);
	}

	public static List<IMethod> getActionMethods(IWebflowConfig config,
			IDOMNode node) {
		IType type = getActionType(config, node);
		if (type != null) {
			if ("bean-action".equals(node.getLocalName())) {
				MethodSearchRequestor requestor = new MethodSearchRequestor(
						null);
				try {
					Set<IMethod> methods = Introspector.getAllMethods(type);
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
					Set<IMethod> methods = Introspector.getAllMethods(type);
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

	public static Set<IBean> getBeans(IWebflowConfig config) {
		Set<IModelElement> beansConfigs = config.getBeansConfigs();
		Set<IBean> beans = new HashSet<IBean>();

		if (beansConfigs != null) {
			for (IModelElement bc : beansConfigs) {
				beans.addAll(BeansModelUtils.getBeans(bc, null));
			}
		}

		return beans;
	}

	public static List<IFile> getFiles(IProject project) {
		IWebflowProject webflowProject = Activator.getModel().getProject(
				project);
		List<IFile> files = new ArrayList<IFile>();
		if (webflowProject != null) {
			for (IWebflowConfig config : webflowProject.getConfigs()) {
				files.add(config.getResource());
			}
		}
		return files;
	}

	public static IWebflowConfig getWebflowConfig(IFile file) {
		IWebflowModel model = Activator.getModel();
		return (file != null && file.getProject() != null
				&& model.getProject(file.getProject()) != null
				&& model.getProject(file.getProject()).getConfig(file) != null ? model
				.getProject(file.getProject()).getConfig(file)
				: null);
	}

	public static IWebflowState getWebflowState(IFile file) {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager()
					.getExistingModelForRead(file);
			if (model == null) {
				model = StructuredModelManager.getModelManager()
						.getModelForRead(file);

			}
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				IWebflowState webflowState = new WebflowState(WebflowModelUtils
						.getWebflowConfig(file));
				webflowState.init((IDOMNode) document.getDocumentElement(),
						null);
				return webflowState;
			}
		}
		catch (Exception e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	public static boolean isReferencedBeanFound(IWebflowConfig config,
			String beanName) {
		Set<IModelElement> beansConfigs = config.getBeansConfigs();
		for (IModelElement beansConfig : beansConfigs) {
			if (BeansModelUtils.getBean(beanName, beansConfig) != null) {
				return true;
			}
		}
		return false;
	}

	public static IWebflowState getWebflowState(IWebflowModelElement element,
			boolean resolveToRoot) {
		if (element != null) {
			if (element instanceof IWebflowState) {
				if (resolveToRoot
						&& element.getElementParent() instanceof InlineFlowState) {
					return getWebflowState((IWebflowModelElement) element
							.getElementParent(), resolveToRoot);
				}
				else {
					return (IWebflowState) element;
				}
			}
			else {
				return getWebflowState((IWebflowModelElement) element
						.getElementParent(), resolveToRoot);
			}
		}
		else {
			return null;
		}
	}

	public static IState getState(IWebflowModelElement element) {
		if (element != null) {
			if (element instanceof IState) {
				return (IState) element;
			}
			else {
				return getState((IWebflowModelElement) element
						.getElementParent());
			}
		}
		else {
			return null;
		}
	}

	public static boolean isWebflowConfig(IResource resource) {
		if (resource instanceof IFile && resource.isAccessible()) {
			return getWebflowConfig((IFile) resource) != null;
		}
		return false;
	}

	public static boolean isStateIdUnique(IState state) {
		IWebflowState webflowState = getWebflowState(state, false);
		List<IState> foundStates = new ArrayList<IState>();
		if (webflowState != null) {
			List<IState> states = new ArrayList<IState>();
			states.addAll(webflowState.getStates());
			states.addAll(webflowState.getInlineFlowStates());
			for (IState s : states) {
				if (s.getId().equals(state.getId())) {
					foundStates.add(s);
				}
			}
		}
		return foundStates.size() == 1;
	}

	public static Set<String> getWebflowConfigNames(IWebflowProject project) {
		Set<String> flowNames = new HashSet<String>();
		for (IWebflowConfig config : project.getConfigs()) {
			flowNames.add(config.getName());
		}
		return flowNames;
	}

	public static Set<String> getWebflowConfigNames(IWebflowState state) {
		Set<String> flowNames = new HashSet<String>();
		for (IInlineFlowState config : state.getInlineFlowStates()) {
			flowNames.add(config.getId());
		}
		return flowNames;
	}
}
