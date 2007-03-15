/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.webflow.ui.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.BeanActionMethodSearchRequestor;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.BeanMethodSearchRequestor;

/**
 * Some helper methods for {@link WebflowEditor}
 * 
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
		IWebflowConfig config = getActiveWebflowConfig();
		Set<IBeansConfig> beansConfigs = config.getBeansConfigs();
		Set<IBean> beans = new HashSet<IBean>();

		if (beansConfigs != null) {
			for (IBeansConfig bc : beansConfigs) {
				beans.addAll(bc.getBeans());
			}
		}

		return beans;
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
				ActionMethodSearchRequestor requestor = new ActionMethodSearchRequestor(null);
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
		Set<String> flowNames = new HashSet<String>();
		for (IWebflowConfig config : project.getConfigs()) {
			flowNames.add(config.getName());
		}
		return flowNames.toArray(new String[flowNames.size()]);
 	}
}