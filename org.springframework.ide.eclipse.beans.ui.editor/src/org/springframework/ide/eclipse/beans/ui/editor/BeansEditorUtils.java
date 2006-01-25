/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BeansEditorUtils {

	public static final List getBeansFromConfigSets(IFile file) {
		List beans = new ArrayList();
		Map configsMap = new HashMap();
		IBeansProject project = BeansCorePlugin.getModel().getProject(
				file.getProject());
		if (project != null) {

			Iterator configSets = project.getConfigSets().iterator();

			while (configSets.hasNext()) {
				IBeansConfigSet configSet = (IBeansConfigSet) configSets.next();
				if (configSet.hasConfig(file)) {
					Iterator configs = configSet.getConfigs().iterator();
					while (configs.hasNext()) {
						String beansConfigName = (String) configs.next();
						IBeansConfig beansConfig = project
								.getConfig(beansConfigName);
						if (beansConfig != null) {
							IResource resource = beansConfig
									.getElementResource();
							if (!configsMap.containsKey(resource.getName())
									&& !resource.getFullPath().equals(
											file.getFullPath())) {
								configsMap.put(resource.getName(), beansConfig);
							}
						}
					}
				}
			}

		}

		Iterator paths = configsMap.keySet().iterator();
		while (paths.hasNext()) {
			IBeansConfig beansConfig = (IBeansConfig) configsMap
					.get((String) paths.next());
			beans.addAll(beansConfig.getBeans());
		}
		return beans;
	}

	public static final boolean isSpringStyleOutline() {
		return BeansEditorPlugin.getDefault().getPreferenceStore().getBoolean(
				IPreferencesConstants.OUTLINE_SPRING);
	}

	public static final String createAdditionalProposalInfo(Node bean,
			IFile file) {
		NamedNodeMap attributes = bean.getAttributes();
		StringBuffer buf = new StringBuffer();
		buf.append("<b>id:</b> ");
		if (attributes.getNamedItem("id") != null) {
			buf.append(attributes.getNamedItem("id").getNodeValue());
		}
		if (attributes.getNamedItem("name") != null) {
			buf.append("<br><b>alias:</b> ");
			buf.append(attributes.getNamedItem("name").getNodeValue());
		}
		buf.append("<br><b>class:</b> ");
		if (attributes.getNamedItem("class") != null) {
			buf.append(attributes.getNamedItem("class").getNodeValue());
		}
		buf.append("<br><b>singleton:</b> ");
		if (attributes.getNamedItem("singleton") != null) {
			buf.append(attributes.getNamedItem("singleton").getNodeValue());
		} else {
			buf.append("true");
		}
		buf.append("<br><b>abstract:</b> ");
		if (attributes.getNamedItem("abstract") != null) {
			buf.append(attributes.getNamedItem("abstract").getNodeValue());
		} else {
			buf.append("false");
		}
		buf.append("<br><b>lazy-init:</b> ");
		if (attributes.getNamedItem("lazy-init") != null) {
			buf.append(attributes.getNamedItem("lazy-init").getNodeValue());
		} else {
			buf.append("default");
		}
		buf.append("<br><b>filename:</b> ");
		buf.append(file.getProjectRelativePath());
		return buf.toString();
	}

	public static final String createAdditionalProposalInfo(IBean bean) {
		StringBuffer buf = new StringBuffer();
		buf.append("<b>id:</b> ");
		buf.append(bean.getElementName());
		if (bean.getAliases() != null && bean.getAliases().length > 0) {
			buf.append("<br><b>alias:</b> ");
			for (int i = 0; i < bean.getAliases().length; i++) {
				buf.append(bean.getAliases()[i]);
				if (i < bean.getAliases().length - 1) {
					buf.append(", ");
				}
			}
		}
		buf.append("<br><b>class:</b> ");
		buf.append(bean.getClassName());
		buf.append("<br><b>singleton:</b> ");
		buf.append(bean.isSingleton());
		buf.append("<br><b>abstract:</b> ");
		buf.append(bean.isAbstract());
		buf.append("<br><b>lazy-init:</b> ");
		buf.append(bean.isLazyInit());
		buf.append("<br><b>filename:</b> ");
		buf.append(bean.getElementResource().getProjectRelativePath());
		return buf.toString();
	}

	public static int getBeanFlags(IBean bean, boolean isExternal) {
		int flags = 0;
		if (isExternal) {
			flags |= BeansModelImageDescriptor.FLAG_IS_EXTERNAL;
		}
		if (!bean.isSingleton()) {
			flags |= BeansModelImageDescriptor.FLAG_IS_PROTOTYPE;
		}
		if (bean.isAbstract()) {
			flags |= BeansModelImageDescriptor.FLAG_IS_ABSTRACT;
		}
		if (bean.isLazyInit()) {
			flags |= BeansModelImageDescriptor.FLAG_IS_LAZY_INIT;
		}
		if (bean.isRootBean() && bean.getClassName() == null
				&& bean.getParentName() == null) {
			flags |= BeansModelImageDescriptor.FLAG_IS_ROOT_BEAN_WITHOUT_CLASS;
		}
		return flags;
	}

	public static List getClassNamesOfBean(IFile file, Node node) {
		List classNames = new ArrayList();
		NamedNodeMap rootAttributes = node.getAttributes();

		String id = (rootAttributes.getNamedItem("id") != null ? rootAttributes
				.getNamedItem("id").getNodeValue() : null);
		if (id == null) {
			id = node.toString();
		}
		String className = (rootAttributes.getNamedItem("class") != null ? rootAttributes
				.getNamedItem("class").getNodeValue()
				: null);
		String parentId = (rootAttributes.getNamedItem("parent") != null ? rootAttributes
				.getNamedItem("parent").getNodeValue()
				: null);

		getClassNamesOfBeans(file, node.getOwnerDocument(), id, className,
				parentId, classNames, new ArrayList());
		return classNames;
	}

	private static void getClassNamesOfBeans(IFile file, Document document,
			String id, String className, String parentId, List classNames,
			List beans) {

		// detect cicular dependencies
		if (id != null) {
			if (beans.contains(id)) {
				return;
			} else {
				beans.add(id);
			}
		} else {
			return;
		}

		if (className != null) {
			IType type = BeansModelUtils.getJavaType(file.getProject(),
					className);
			if (type != null && !classNames.contains(type)) {
				classNames.add(type);
			}

		}

		if (parentId != null) {
			boolean foundLocal = false;

			NodeList beanNodes = document.getElementsByTagName("bean");
			for (int i = 0; i < beanNodes.getLength(); i++) {
				Node beanNode = beanNodes.item(i);
				NamedNodeMap attributes = beanNode.getAttributes();
				if (attributes.getNamedItem("id") != null) {
					String idTemp = (attributes.getNamedItem("id") != null ? attributes
							.getNamedItem("id").getNodeValue()
							: null);
					String classNameTemp = (attributes.getNamedItem("class") != null ? attributes
							.getNamedItem("class").getNodeValue()
							: null);
					String parentIdTemp = (attributes.getNamedItem("parent") != null ? attributes
							.getNamedItem("parent").getNodeValue()
							: null);

					if (parentId.equals(idTemp)) {
						foundLocal = true;
						getClassNamesOfBeans(file, document, idTemp,
								classNameTemp, parentIdTemp, classNames, beans);
					}
				}
			}
			if (!foundLocal) {
				List beansList = BeansEditorUtils.getBeansFromConfigSets(file);
				for (int i = 0; i < beansList.size(); i++) {
					IBean bean = (IBean) beansList.get(i);

					if (parentId.equals(bean.getElementName())) {
						getClassNamesOfBeans(file, document, bean
								.getElementName(), bean.getClassName(), bean
								.getParentName(), classNames, beans);
						break;
					}
				}
			}
		}
	}

	public static String getClassNameForBean(IFile file, Document document,
			String id) {

		boolean foundLocal = false;

		NodeList beanNodes = document.getElementsByTagName("bean");
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			NamedNodeMap attributes = beanNode.getAttributes();
			if (attributes.getNamedItem("id") != null) {
				String idTemp = (attributes.getNamedItem("id") != null ? attributes
						.getNamedItem("id").getNodeValue()
						: null);
				String classNameTemp = (attributes.getNamedItem("class") != null ? attributes
						.getNamedItem("class").getNodeValue()
						: null);

				if (id.equals(idTemp)) {
					foundLocal = true;
					return classNameTemp;
				}
			}
		}
		if (!foundLocal) {
			List beansList = BeansEditorUtils.getBeansFromConfigSets(file);
			for (int i = 0; i < beansList.size(); i++) {
				IBean bean = (IBean) beansList.get(i);
				if (id.equals(bean.getElementName())) {
					return bean.getClassName();
				}
			}
		}

		return null;
	}

	/**
	 * Returns the non-blocking Progress Monitor form the StatuslineManger
	 * 
	 * @return the progress monitor
	 */
	public static IProgressMonitor getProgressMonitor() {
		IEditorPart editor = BeansEditorPlugin.getActiveWorkbenchPage()
				.getActiveEditor();
		if (editor != null
				&& editor.getEditorSite() != null
				&& editor.getEditorSite().getActionBars() != null
				&& editor.getEditorSite().getActionBars()
						.getStatusLineManager() != null
				&& editor.getEditorSite().getActionBars()
						.getStatusLineManager().getProgressMonitor() != null) {

			IStatusLineManager manager = editor.getEditorSite().getActionBars()
					.getStatusLineManager();
			IProgressMonitor monitor = manager.getProgressMonitor();
			manager.setMessage("Processing completion proposals");
			manager.setCancelEnabled(true);
			return monitor;
		} else {

			return new NullProgressMonitor();
		}
	}

	public static IType getTypeForMethodReturnType(IMethod method,
			IType contextType, IFile file) {
		IType returnType = null;
		try {
			String returnTypeString = Signature
					.toString(method.getReturnType()).replace('$', '.');
			returnType = BeansModelUtils.getJavaType(file.getProject(),
					resolveClassName(returnTypeString, contextType));
		} catch (IllegalArgumentException e) {
			// do Nothing
		} catch (JavaModelException e) {
			// do Nothing
		}
		return returnType;
	}

	public static String resolveClassName(String className, IType type) {
		try {
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		} catch (JavaModelException e) {
		}

		return className;
	}

}
