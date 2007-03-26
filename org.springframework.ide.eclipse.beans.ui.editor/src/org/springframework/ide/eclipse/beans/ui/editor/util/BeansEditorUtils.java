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
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.springframework.ide.eclipse.beans.ui.editor.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IReferenceableElementsLocator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.ui.editors.ZipEntryEditorInput;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collection of helper methods for beans XML editor.
 * 
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansEditorUtils {

	protected static final String AMPERSTAND = "&"; //$NON-NLS-1$

	protected static final String AMPERSTAND_ENTITY = "&&;"; //$NON-NLS-1$

	protected static final String CARRIAGE_RETURN = "\r"; //$NON-NLS-1$

	protected static final String CARRIAGE_RETURN_ENTITY = "\\r"; //$NON-NLS-1$

	protected static final String CR = "\r"; //$NON-NLS-1$

	protected static final String CRLF = "\r\n"; //$NON-NLS-1$

	protected static final String DELIMITERS = " \t\n\r\f"; //$NON-NLS-1$

	protected static final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$

	protected static final char DOUBLE_QUOTE_CHAR = '\"'; 

	protected static final String DOUBLE_QUOTE_ENTITY = "&quot;"; //$NON-NLS-1$

	protected static final String EQUAL_SIGN = "="; //$NON-NLS-1$

	protected static final String EQUAL_SIGN_ENTITY = "&#61;"; //$NON-NLS-1$

	protected static final String GREATER_THAN = ">"; //$NON-NLS-1$

	protected static final String GREATER_THAN_ENTITY = "&gt;"; //$NON-NLS-1$

	protected static final String LESS_THAN = "<"; //$NON-NLS-1$

	protected static final String LESS_THAN_ENTITY = "&lt;"; //$NON-NLS-1$

	protected static final String LF = "\n"; //$NON-NLS-1$

	protected static final String LINE_FEED = "\n"; //$NON-NLS-1$

	protected static final String LINE_FEED_ENTITY = "\\n"; //$NON-NLS-1$

	protected static final String LINE_FEED_TAG = "<dl>"; //$NON-NLS-1$

	protected static final String LINE_TAB = "\t"; //$NON-NLS-1$

	protected static final String LINE_TAB_ENTITY = "\\t"; //$NON-NLS-1$

	protected static final String LINE_TAB_TAG = "<dd>"; //$NON-NLS-1$

	protected static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$

	protected static final char SINGLE_QUOTE_CHAR = '\''; 

	protected static final String SINGLE_QUOTE_ENTITY = "&#039;"; //$NON-NLS-1$

	protected static final String SPACE = " "; //$NON-NLS-1$

	protected static final String SPACE_ENTITY = "&nbsp;"; //$NON-NLS-1$

	/**
	 * Replace matching literal portions of a string with another string
	 */
	public static final String replace(String aString, String source,
			String target) {
		if (aString == null)
			return null;
		String normalString = ""; //$NON-NLS-1$
		int length = aString.length();
		int position = 0;
		int previous = 0;
		int spacer = source.length();
		while (position + spacer - 1 < length
				&& aString.indexOf(source, position) > -1) {
			position = aString.indexOf(source, previous);
			normalString = normalString + aString.substring(previous, position)
					+ target;
			position += spacer;
			previous = position;
		}
		normalString = normalString
				+ aString.substring(position, aString.length());

		return normalString;
	}

	public static String convertToHTMLContent(String content) {
		content = replace(content, AMPERSTAND, AMPERSTAND_ENTITY);
		content = replace(content, LESS_THAN, LESS_THAN_ENTITY);
		content = replace(content, GREATER_THAN, GREATER_THAN_ENTITY);
		content = replace(content, LINE_FEED, LINE_FEED_TAG);
		content = replace(content, LINE_TAB, LINE_TAB_TAG);
		content = replace(content, SINGLE_QUOTE, SINGLE_QUOTE_ENTITY);
		content = replace(content, DOUBLE_QUOTE, DOUBLE_QUOTE_ENTITY);
		content = replace(content, SPACE + SPACE, SPACE_ENTITY + SPACE_ENTITY); // replacing
		return content;
	}

	public static final List getBeansFromConfigSets(IFile file) {
		List<IBean> beans = new ArrayList<IBean>();
		Map<String, IBeansConfig> configsMap = new HashMap<String, IBeansConfig>();
		IBeansProject project = BeansCorePlugin.getModel().getProject(
				file.getProject());
		if (project != null) {
			Set<IBeansConfigSet> configSets = project.getConfigSets();

			for (IBeansConfigSet configSet : configSets) {
				if (configSet.hasConfig(file)
						|| !BeansCoreUtils.isBeansConfig(file)) {
					Set<IBeansConfig> configs = configSet.getConfigs();
					for (IBeansConfig beansConfig : configs) {
						if (beansConfig != null) {
							IResource resource = beansConfig
									.getElementResource();
							if (resource != null
									&& !configsMap.containsKey(resource
											.getName())
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
			IBeansConfig beansConfig = configsMap
					.get(paths.next());
			beans.addAll(beansConfig.getBeans());
			Set<IBeansComponent> components = beansConfig.getComponents();
			for (IBeansComponent component : components) {
				beans.addAll(component.getBeans());
			}
		}
		return beans;
	}

	public static final boolean isSpringStyleOutline() {
		return Activator.getDefault().getPreferenceStore().getBoolean(
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
		}
		else {
			buf.append("true");
		}
		buf.append("<br><b>abstract:</b> ");
		if (attributes.getNamedItem("abstract") != null) {
			buf.append(attributes.getNamedItem("abstract").getNodeValue());
		}
		else {
			buf.append("false");
		}
		buf.append("<br><b>lazy-init:</b> ");
		if (attributes.getNamedItem("lazy-init") != null) {
			buf.append(attributes.getNamedItem("lazy-init").getNodeValue());
		}
		else {
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

	public static final List getClassNamesOfBean(IFile file, Node node) {
		List<IType> classNames = new ArrayList<IType>();
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
				parentId, classNames, new ArrayList<String>());
		return classNames;
	}

	private static final void getClassNamesOfBeans(IFile file,
			Document document, String id, String className, String parentId,
			List<IType> classNames, List<String> beans) {

		// detect cicular dependencies
		if (id != null) {
			if (beans.contains(id)) {
				return;
			}
			else {
				beans.add(id);
			}
		}
		else {
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
								.getElementName(), BeansModelUtils.getBeanClass(bean, null), bean
								.getParentName(), classNames, beans);
						break;
					}
				}
			}
		}
	}

	public static final String getClassNameForBean(IFile file,
			Document document, String id) {

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
					return BeansModelUtils.getBeanClass(bean, null);
				}
			}
		}

		// if we reach this point we haven't found a class name. so try to
		// locate the xml element
		// and calculate the class from there
		Element node = document.getElementById(id);
		if (node != null
				&& node.getNamespaceURI() != null
				&& NamespaceUtils.getClassNameProvider(node.getNamespaceURI()) != null) {
			return NamespaceUtils.getClassNameProvider(node.getNamespaceURI())
					.getClassNameForElement(node);
		}

		return null;
	}

	/**
	 * Returns the non-blocking Progress Monitor form the StatuslineManger
	 * 
	 * @return the progress monitor
	 */
	public static final IProgressMonitor getProgressMonitor() {
		IEditorPart editor = Activator.getActiveWorkbenchPage()
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
		}
		else {

			return new NullProgressMonitor();
		}
	}

	public static final IType getTypeForMethodReturnType(IMethod method,
			IType contextType) {
		IType returnType = null;
		try {
			returnType = getTypeFromSignature(method.getReturnType(),
					contextType);
		}
		catch (JavaModelException e) {
		}
		return returnType;
	}

	private static IType getTypeFromSignature(String string, IType contextType) {
		IType returnType = null;
		try {
			String returnTypeString = Signature.toString(string).replace('$',
					'.');
			returnType = BeansModelUtils.getJavaType(contextType
					.getJavaProject().getProject(), resolveClassName(
					returnTypeString, contextType));
		}
		catch (IllegalArgumentException e) {
			// do Nothing
		}
		return returnType;
	}

	@SuppressWarnings("unchecked")
	public static final List<IType> getTypeForMethodParameterTypes(
			IMethod method, IType contextType) {
		if (method == null || method.getParameterTypes() == null
				|| method.getParameterTypes().length == 0) {
			return Collections.EMPTY_LIST;
		}
		List<IType> parameterTypes = new ArrayList<IType>(method
				.getParameterTypes().length);
		String[] parameterTypeNames = method.getParameterTypes();
		for (String parameterTypeName : parameterTypeNames) {
			parameterTypes.add(getTypeFromSignature(parameterTypeName,
					contextType));
		}
		return parameterTypes;
	}

	public static final String resolveClassName(String className, IType type) {
		try {
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		}
		catch (JavaModelException e) {
		}

		return className;
	}

	public static final IRegion extractPropertyPathFromCursorPosition(
			IRegion hyperlinkRegion, IRegion cursor, String target,
			List<String> propertyPaths) {

		int cursorIndexInTarget = cursor.getOffset()
				- hyperlinkRegion.getOffset();

		if (cursorIndexInTarget > 0 && cursorIndexInTarget < target.length()) {

			String preTarget = target.substring(0, cursorIndexInTarget);
			if (!preTarget.endsWith(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)) {
				int regionOffset = hyperlinkRegion.getOffset()
						+ preTarget
								.lastIndexOf(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)
						+ 1;
				int segmentCount = new StringTokenizer(preTarget,
						PropertyAccessor.NESTED_PROPERTY_SEPARATOR)
						.countTokens();
				StringTokenizer tok = new StringTokenizer(target,
						PropertyAccessor.NESTED_PROPERTY_SEPARATOR);

				for (int i = 0; i < segmentCount; i++) {
					propertyPaths.add(tok.nextToken());
				}

				int regionLength = (propertyPaths
						.get(segmentCount - 1)).length();

				return new Region(regionOffset, regionLength);
			}
		}

		return hyperlinkRegion;

	}

	public static final IMethod extractMethodFromPropertyPathElements(
			List propertyPathElements, List types, IFile file, int counter) {
		IMethod method = null;
		if (propertyPathElements != null && propertyPathElements.size() > 0) {
			if (propertyPathElements.size() > (counter + 1)) {

				if (types != null) {
					IType returnType = null;
					for (int i = 0; i < types.size(); i++) {
						IType type = (IType) types.get(i);
						String propertyPath = (String) propertyPathElements
								.get(counter);
						try {
							IMethod getMethod = Introspector
									.getReadableProperty(
											type,
											PropertyAccessorUtils
													.getPropertyName(propertyPath));
							returnType = BeansEditorUtils
									.getTypeForMethodReturnType(getMethod, type);
						}
						catch (JavaModelException e) {
						}
					}

					if (returnType != null) {
						List<IType> newTypes = new ArrayList<IType>();
						newTypes.add(returnType);
						method = extractMethodFromPropertyPathElements(
								propertyPathElements, newTypes, file,
								(counter + 1));
					}
				}
			}
			else {
				for (int i = 0; i < types.size(); i++) {
					IType type = (IType) types.get(i);
					String propertyPath = (String) propertyPathElements
							.get(counter);
					try {
						method = Introspector.getWritableProperty(type,
								PropertyAccessorUtils
										.getPropertyName(propertyPath));
					}
					catch (JavaModelException e) {
					}
				}
			}
		}
		return method;
	}

	public static final void extractAllMethodsFromPropertyPathElements(
			List propertyPath, List types, IFile file, int counter,
			List<IMethod> methods) {
		IMethod method = null;
		if (propertyPath != null && propertyPath.size() > 0) {
			if (propertyPath.size() > (counter + 1)) {

				if (types != null) {
					IType returnType = null;
					for (int i = 0; i < types.size(); i++) {
						IType type = (IType) types.get(i);
						try {
							IMethod getMethod = Introspector
									.getReadableProperty(type,
											(String) propertyPath.get(counter));
							returnType = BeansEditorUtils
									.getTypeForMethodReturnType(getMethod, type);
							methods.add(getMethod);
						}
						catch (JavaModelException e) {
						}
					}

					if (returnType != null) {
						List<IType> newTypes = new ArrayList<IType>();
						newTypes.add(returnType);
						extractAllMethodsFromPropertyPathElements(propertyPath,
								newTypes, file, (counter + 1), methods);
					}

				}
			}
			else {
				for (int i = 0; i < types.size(); i++) {
					IType type = (IType) types.get(i);
					try {
						method = Introspector.getWritableProperty(type,
								(String) propertyPath.get(counter));
						methods.add(method);

					}
					catch (JavaModelException e) {
					}
				}
			}
		}
	}

	/**
	 * Returns the node from given document at specified offset.
	 * 
	 * @param offset the offset with given document
	 * @return Node either element, doctype, text, or null
	 */
	public static final Node getNodeByOffset(IDocument document, int offset) {
		// get the node at offset (returns either: element, doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = org.eclipse.wst.sse.core.StructuredModelManager
					.getModelManager().getExistingModelForRead(document);
			inode = sModel.getIndexedRegion(offset);
			if (inode == null)
				inode = sModel.getIndexedRegion(offset - 1);
		}
		finally {
			if (sModel != null)
				sModel.releaseFromRead();
		}

		if (inode instanceof Node) {
			return (Node) inode;
		}
		return null;
	}

	/**
	 * Returns the attribute from given node at specified offset.
	 */
	public static final Attr getAttrByOffset(Node node, int offset) {
		if ((node instanceof IndexedRegion)
				&& ((IndexedRegion) node).contains(offset)
				&& (node.hasAttributes())) {
			NamedNodeMap attrs = node.getAttributes();
			// go through each attribute in node and if attribute contains
			// offset, return that attribute
			for (int i = 0; i < attrs.getLength(); ++i) {
				// assumption that if parent node is of type IndexedRegion,
				// then its attributes will also be of type IndexedRegion
				IndexedRegion attRegion = (IndexedRegion) attrs.item(i);
				if (attRegion.contains(offset)) {
					return (Attr) attrs.item(i);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the file the given document was read from.
	 */
	public static final IFile getFile(IDocument document) {
		IFile resource = null;
		String baselocation = null;

		if (document != null) {
			IStructuredModel model = null;
			try {
				model = org.eclipse.wst.sse.core.StructuredModelManager
						.getModelManager().getExistingModelForRead(document);
				if (model != null) {
					baselocation = model.getBaseLocation();
				}
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}

		if (baselocation != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baselocation);
			if (filePath.segmentCount() > 0) {
				resource = root.getFile(filePath);
			}
		}
		return resource;
	}

	public static final IProject getProject(IDocument document) {
		IProject project = null;
		IEditorInput input = Workbench.getInstance().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor().getEditorInput();
		if (input instanceof ZipEntryEditorInput) {
			ZipEntryStorage storage = (ZipEntryStorage) ((ZipEntryEditorInput) input)
					.getStorage();
			project = storage.getFile().getProject();
		}
		else {
			IFile file = BeansEditorUtils.getFile(document);
			project = file.getProject();
		}
		return project;
	}

	/**
	 * Convert <code>String</code>s in attribute name format (lowercase,
	 * hyphens separating words) into property name format (camel-cased). For
	 * example, <code>transaction-manager</code> is converted into
	 * <code>transactionManager</code>.
	 */
	public static String attributeNameToPropertyName(String attributeName) {
		if (attributeName.indexOf("-") == -1) {
			return attributeName;
		}
		char[] chars = attributeName.toCharArray();
		char[] result = new char[chars.length - 1];

		int currPos = 0;
		boolean upperCaseNext = false;
		for (char c : chars) {
			if (c == '-') {
				upperCaseNext = true;
				continue;
			}
			else if (upperCaseNext) {
				result[currPos++] = Character.toUpperCase(c);
				upperCaseNext = false;
			}
			else {
				result[currPos++] = c;
			}
		}
		return new String(result, 0, currPos);
	}

	public static final String propertyNameToAttributeName(String propertyName) {
		char[] chars = propertyName.toCharArray();
		StringBuffer buf = new StringBuffer();

		for (char c : chars) {
			if (Character.isUpperCase(c)) {
				c = Character.toLowerCase(c);
				buf.append('-');
				buf.append(c);
			}
			else {
				buf.append(c);
			}
		}
		return buf.toString();
	}

	public static final String getAttribute(Node node, String attributeName) {
		if (hasAttribute(node, attributeName)) {
			return node.getAttributes().getNamedItem(attributeName)
					.getNodeValue();
		}
		return null;
	}

	public static final boolean hasAttribute(Node node, String attributeName) {
		return (node != null && node.hasAttributes() && node.getAttributes()
				.getNamedItem(attributeName) != null);
	}

	public static final Map<String, Node> getReferenceableNodes(
			Document document) {
		Map<String, Node> nodes = new HashMap<String, Node>();
		for (IReferenceableElementsLocator locator : NamespaceUtils
				.getAllElementsLocators()) {

			Map<String, Node> tempNodes = locator
					.getReferenceableElements(document);
			if (tempNodes != null) {
				nodes.putAll(tempNodes);
			}
		}
		return nodes;
	}

	public static final Node getFirstReferenceableNodeById(Document document,
			String id) {
		Map<String, Node> nodes = getReferenceableNodes(document);
		for (Map.Entry<String, Node> node : nodes.entrySet()) {
			if (node.getKey().equals(id)) {
				return node.getValue();
			}
		}
		return null;
	}

	public static String getClassNameForBean(Node bean) {
		return getAttribute(bean, "class");
	}

	public static IFile getResource(ContentAssistRequest request) {
		IResource resource = null;
		String baselocation = null;

		if (request != null) {
			IStructuredDocumentRegion region = request.getDocumentRegion();
			if (region != null) {
				IDocument document = region.getParentDocument();
				IStructuredModel model = null;
				try {
					model = org.eclipse.wst.sse.core.StructuredModelManager
							.getModelManager()
							.getExistingModelForRead(document);
					if (model != null) {
						baselocation = model.getBaseLocation();
					}
				}
				finally {
					if (model != null) {
						model.releaseFromRead();
					}
				}
			}
		}

		if (baselocation != null) {
			// copied from JSPTranslationAdapter#getJavaProject
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baselocation);
			if (filePath.segmentCount() > 0) {
				resource = root.getFile(filePath);
			}
		}
		return (IFile) resource;
	}
}
