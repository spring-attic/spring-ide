/*******************************************************************************
 * Copyright (c) 2005, 2013 Spring IDE Developers
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jdt.internal.ui.text.java.ProposalInfo;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.springframework.ide.eclipse.beans.ui.editor.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IClassNameProvider;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IReferenceableElementsLocator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.editors.ZipEntryEditorInput;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Collection of helper methods for beans XML editor.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Terry Denney
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
	 * Convert <code>String</code>s in attribute name format (lowercase, hyphens separating words) into property name
	 * format (camel-cased). For example, <code>transaction-manager</code> is converted into
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

	public static final String createAdditionalProposalInfo(Node bean, IFile file) {
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
		if (file != null) {
			buf.append("<br><b>filename:</b> ");
			buf.append(file.getProjectRelativePath());
		}
		return buf.toString();
	}

	public static final String createAdditionalProposalInfo(Object obj, IProgressMonitor monitor) {
		if (obj instanceof IBean) {
			return createAdditionalProposalInfo((IBean) obj);
		}
		else if (obj instanceof ElementImpl) {
			ElementImpl node = (ElementImpl) obj;
			IStructuredDocument document = node.getStructuredDocument();
			IFile resource = getFile(document);
			return createAdditionalProposalInfo((Node) obj, resource);
		}
		else if (obj instanceof IMember) {
			return new ProposalInfo((IMember) obj).getInfo(monitor);
		}
		return null;
	}

	public static final void extractAllMethodsFromPropertyPathElements(List propertyPath, List types, IFile file,
			int counter, List<IMethod> methods) {
		IMethod method = null;
		if (propertyPath != null && propertyPath.size() > 0) {
			if (propertyPath.size() > (counter + 1)) {

				if (types != null) {
					IType returnType = null;
					for (int i = 0; i < types.size(); i++) {
						IType type = (IType) types.get(i);
						try {
							IMethod getMethod = Introspector.getReadableProperty(type,
									(String) propertyPath.get(counter));
							returnType = JdtUtils.getJavaTypeForMethodReturnType(getMethod, type);
							methods.add(getMethod);
						}
						catch (JavaModelException e) {
						}
					}

					if (returnType != null) {
						List<IType> newTypes = new ArrayList<IType>();
						newTypes.add(returnType);
						extractAllMethodsFromPropertyPathElements(propertyPath, newTypes, file, (counter + 1), methods);
					}

				}
			}
			else {
				for (int i = 0; i < types.size(); i++) {
					IType type = (IType) types.get(i);
					try {
						method = Introspector.getWritableProperty(type, (String) propertyPath.get(counter));
						methods.add(method);

					}
					catch (JavaModelException e) {
					}
				}
			}
		}
	}

	public static final IMethod extractMethodFromPropertyPathElements(List propertyPathElements, List types,
			IFile file, int counter) {
		IMethod method = null;
		if (propertyPathElements != null && propertyPathElements.size() > 0) {
			if (propertyPathElements.size() > (counter + 1)) {

				if (types != null) {
					IType returnType = null;
					for (int i = 0; i < types.size(); i++) {
						IType type = (IType) types.get(i);
						String propertyPath = (String) propertyPathElements.get(counter);
						try {
							IMethod getMethod = Introspector.getReadableProperty(type,
									PropertyAccessorUtils.getPropertyName(propertyPath));
							returnType = JdtUtils.getJavaTypeForMethodReturnType(getMethod, type);
						}
						catch (JavaModelException e) {
						}
					}

					if (returnType != null) {
						List<IType> newTypes = new ArrayList<IType>();
						newTypes.add(returnType);
						method = extractMethodFromPropertyPathElements(propertyPathElements, newTypes, file,
								(counter + 1));
					}
				}
			}
			else {
				for (int i = 0; i < types.size(); i++) {
					IType type = (IType) types.get(i);
					String propertyPath = (String) propertyPathElements.get(counter);
					try {
						method = Introspector.getWritableProperty(type,
								PropertyAccessorUtils.getPropertyName(propertyPath));
					}
					catch (JavaModelException e) {
					}
				}
			}
		}
		return method;
	}

	public static final IRegion extractPropertyPathFromCursorPosition(IRegion hyperlinkRegion, IRegion cursor,
			String target, List<String> propertyPaths) {

		int cursorIndexInTarget = cursor.getOffset() - hyperlinkRegion.getOffset();

		if (cursorIndexInTarget > 0 && cursorIndexInTarget < target.length()) {

			String preTarget = target.substring(0, cursorIndexInTarget);
			if (!preTarget.endsWith(PropertyAccessor.NESTED_PROPERTY_SEPARATOR)) {
				int regionOffset = hyperlinkRegion.getOffset()
						+ preTarget.lastIndexOf(PropertyAccessor.NESTED_PROPERTY_SEPARATOR) + 1;
				int segmentCount = new StringTokenizer(preTarget, PropertyAccessor.NESTED_PROPERTY_SEPARATOR)
						.countTokens();
				StringTokenizer tok = new StringTokenizer(target, PropertyAccessor.NESTED_PROPERTY_SEPARATOR);

				for (int i = 0; i < segmentCount; i++) {
					propertyPaths.add(tok.nextToken());
				}

				int regionLength = (propertyPaths.get(segmentCount - 1)).length();

				return new Region(regionOffset, regionLength);
			}
		}

		return hyperlinkRegion;

	}

	/**
	 * Returns the attribute from given node at specified offset.
	 */
	public static final Attr getAttrByOffset(Node node, int offset) {
		if ((node instanceof IndexedRegion) && ((IndexedRegion) node).contains(offset) && (node.hasAttributes())) {
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

	public static final boolean isElementAtOffset(Node node, int offset) {
		IndexedRegion region = ((IndexedRegion) node);
		int nameLength = node.getNodeName().length();
		return (region.getStartOffset() + nameLength) >= offset && region.getStartOffset() <= offset;
	}

	/**
	 * /** Returns the closest IndexedRegion for the offset and viewer allowing for differences between viewer offsets
	 * and model positions. note: this method returns an IndexedRegion for read only
	 * <p>
	 * Copied from {@link ContentAssistUtils} in order to solve compatibility problems on 3.2.
	 * @see ContentAssistUtils#getNodeAt(ITextViewer, int)
	 */
	public static IndexedRegion getNodeAt(ITextViewer viewer, int documentOffset) {

		if (viewer == null)
			return null;

		IndexedRegion node = null;
		IModelManager mm = StructuredModelManager.getModelManager();
		IStructuredModel model = null;
		if (mm != null)
			model = mm.getExistingModelForRead(viewer.getDocument());
		try {
			if (model != null) {
				int lastOffset = documentOffset;
				node = model.getIndexedRegion(documentOffset);
				while (node == null && lastOffset >= 0) {
					lastOffset--;
					node = model.getIndexedRegion(lastOffset);
				}
			}
		}
		finally {
			if (model != null)
				model.releaseFromRead();
		}
		return node;
	}

	public static final String getAttribute(Node node, String attributeName) {
		if (hasAttribute(node, attributeName)) {
			return node.getAttributes().getNamedItem(attributeName).getNodeValue();
		}
		return null;
	}

	public static final Set<IBean> getBeansFromConfigSets(IFile file) {
		Set<IBean> beans = new HashSet<IBean>();
		Set<IBeansConfig> configs = new HashSet<IBeansConfig>();
		IBeansProject project = BeansCorePlugin.getModel().getProject(file.getProject());
		BeansConfigId id = BeansConfigId.create(file, project.getProject());
        Set<IBeansConfig> allConfigs = BeansCorePlugin.getModel().getConfigs(file, true);
		for (IBeansConfig config : allConfigs) {
			if (config instanceof IImportedBeansConfig) {
				IBeansConfig rootBeansConfig = BeansModelUtils.getParentOfClass(config, IBeansConfig.class);
				configs.add(rootBeansConfig);
			}
		}

		if (project != null) {
			Set<IBeansConfigSet> configSets = project.getConfigSets();

			for (IBeansConfigSet configSet : configSets) {
				if (configSet.hasConfig(id) || !BeansCoreUtils.isBeansConfig(file)) {
					Set<IBeansConfig> bcs = configSet.getConfigs();
					configs.addAll(bcs);
				}
				Set<IBeansConfig> tempConfigs = new HashSet<IBeansConfig>(configs);
				for (IBeansConfig config : tempConfigs) {
					if (configSet.hasConfig(config.getId())) {
						Set<IBeansConfig> bcs = configSet.getConfigs();
						configs.addAll(bcs);
					}
				}
			}
		}

		if (BeansCoreUtils.isBeansConfig(file, true)) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(id);
			if (config instanceof IImportedBeansConfig) {
				configs.add(BeansModelUtils.getParentOfClass(config, IBeansConfig.class));
			}
			else {
				configs.add(config);
			}
		}

		for (IBeansConfig bc : configs) {
			Set<IBean> bs = bc.getBeans();
			for (IBean b : bs) {
				if (!b.getElementResource().equals(file)) {
					beans.add(b);
				}
			}
 			
			Set<IBeansComponent> components = bc.getComponents();
			for (IBeansComponent component : components) {
				getBeansFromComponent(file, component, beans);
			}
		}
		return beans;
	}

	private static void getBeansFromComponent(IFile file, IBeansComponent component, Set<IBean> beansAcc) {
		Set<IBean> bs = component.getBeans();
		for (IBean b : bs) {
			if (!b.getElementResource().equals(file)) {
				beansAcc.add(b);
			}
		}
		
		Set<IBeansComponent> childComponents = component.getComponents();
		for (IBeansComponent childComponent : childComponents) {
			getBeansFromComponent(file, childComponent, beansAcc);
		}
	}
	
	public static final String getClassNameForBean(IFile file, Document document, Node node) {

		NamedNodeMap attributes = node.getAttributes();
		String className = (attributes.getNamedItem("class") != null ? attributes.getNamedItem("class").getNodeValue()
				: null);
		String factoryMethod = (attributes.getNamedItem("factory-method") != null ? attributes.getNamedItem(
				"factory-method").getNodeValue() : null);
		String factoryBean = (attributes.getNamedItem("factory-bean") != null ? attributes.getNamedItem("factory-bean")
				.getNodeValue() : null);
		if (!org.springframework.util.StringUtils.hasText(factoryBean)) {
			factoryBean = (attributes.getNamedItem("factory-ref") != null ? attributes.getNamedItem("factory-ref")
					.getNodeValue() : null);
		}
		String parent = (attributes.getNamedItem("parent") != null ? attributes.getNamedItem("parent").getNodeValue()
				: null);
		if (factoryBean == null && factoryMethod == null && parent == null) {
			return className;
		}
		else if (className != null && factoryMethod != null && factoryBean == null) {
			// Factory method on bean class
			return resolveClassNameFromFactoryMethod(factoryMethod, className, file.getProject());
		}
		else if (factoryMethod != null && factoryBean != null) {
			// Factory method on factory bean
			String factoryClass = getClassNameForBean(file, document, factoryBean);
			if (factoryClass != null) {
				return resolveClassNameFromFactoryMethod(factoryMethod, factoryClass, file.getProject());
			}
			return null;

		}
		else if (className == null && parent != null) {
			return getClassNameForBean(file, document, parent);
		}
		else if (className != null) {
			return className;
		}
		return null;
	}

	public static final String getClassNameForBean(IFile file, Document document, String id) {
		boolean foundLocal = false;

		NodeList beanNodes = document.getElementsByTagNameNS(NamespaceUtils.DEFAULT_NAMESPACE_URI, "bean");
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			NamedNodeMap attributes = beanNode.getAttributes();
			if (attributes.getNamedItem("id") != null) {
				String idTemp = (attributes.getNamedItem("id") != null ? attributes.getNamedItem("id").getNodeValue()
						: null);
				if (id != null && id.equals(idTemp)) {
					return getClassNameForBean(file, document, beanNode);
				}
			}
		}

		if (!foundLocal) {
			Set<IBean> beansList = BeansEditorUtils.getBeansFromConfigSets(file);
			Iterator<IBean> iterator = beansList.iterator();
			while (iterator.hasNext()) {
				IBean bean = iterator.next();
				if (id != null && id.equals(bean.getElementName())) {
					return BeansModelUtils.getBeanClass(bean, null);
				}
			}
		}

		// if we reach this point we haven't found a class name. so try to
		// locate the xml element
		// and calculate the class from there
		Element node = document.getElementById(id);
		if (node != null && node.getNamespaceURI() != null
				&& NamespaceUtils.getClassNameProvider(node.getNamespaceURI()).length > 0) {
			for (IClassNameProvider provider : NamespaceUtils.getClassNameProvider(node.getNamespaceURI())) {
				String className = provider.getClassNameForElement(node);
				if (className != null) {
					return className;
				}
			}
		}

		return null;
	}

	public static String getClassNameForBean(Node bean) {
		return getAttribute(bean, "class");
	}

	public static final List<IType> getClassNamesOfBean(IFile file, Node node) {
		List<IType> classNames = new ArrayList<IType>();
		NamedNodeMap rootAttributes = node.getAttributes();

		String id = (rootAttributes.getNamedItem("id") != null ? rootAttributes.getNamedItem("id").getNodeValue()
				: null);
		if (id == null) {
			id = node.toString();
		}
		String className = (rootAttributes.getNamedItem("class") != null ? rootAttributes.getNamedItem("class")
				.getNodeValue() : null);
		String parentId = (rootAttributes.getNamedItem("parent") != null ? rootAttributes.getNamedItem("parent")
				.getNodeValue() : null);

		getClassNamesOfBeans(file, node.getOwnerDocument(), id, className, parentId, classNames,
				new ArrayList<String>());
		return classNames;
	}

	private static final void getClassNamesOfBeans(IFile file, Document document, String id, String className,
			String parentId, List<IType> classNames, List<String> beans) {

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
			IType type = JdtUtils.getJavaType(file.getProject(), className);
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
					String idTemp = (attributes.getNamedItem("id") != null ? attributes.getNamedItem("id")
							.getNodeValue() : null);
					String classNameTemp = (attributes.getNamedItem("class") != null ? attributes.getNamedItem("class")
							.getNodeValue() : null);
					String parentIdTemp = (attributes.getNamedItem("parent") != null ? attributes
							.getNamedItem("parent").getNodeValue() : null);

					if (parentId.equals(idTemp)) {
						foundLocal = true;
						getClassNamesOfBeans(file, document, idTemp, classNameTemp, parentIdTemp, classNames, beans);
					}
				}
			}
			if (!foundLocal) {
				Set<IBean> beansList = BeansEditorUtils.getBeansFromConfigSets(file);
				Iterator<IBean> iterator = beansList.iterator();
				while (iterator.hasNext()) {
					IBean bean = iterator.next();
					if (parentId.equals(bean.getElementName())) {
						getClassNamesOfBeans(file, document, bean.getElementName(),
								BeansModelUtils.getBeanClass(bean, null), bean.getParentName(), classNames, beans);
						break;
					}
				}
			}
		}
	}

	public static IFile getFile(ContentAssistRequest request) {
		IResource resource = null;
		String baselocation = null;

		if (request != null) {
			IStructuredDocumentRegion region = request.getDocumentRegion();
			if (region != null) {
				IDocument document = region.getParentDocument();
				IStructuredModel model = null;
				try {
					model = org.eclipse.wst.sse.core.StructuredModelManager.getModelManager().getExistingModelForRead(
							document);
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

	/**
	 * Returns the file the given document was read from.
	 */
	public static final IFile getFile(IDocument document) {
		IFile resource = null;
		String baselocation = null;
		if (document != null) {
			IStructuredModel model = null;
			try {
				model = org.eclipse.wst.sse.core.StructuredModelManager.getModelManager().getExistingModelForRead(
						document);
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
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (baselocation != null) {
			IPath path = new Path(baselocation);
			if (root.exists(path) && path.segmentCount() > 1) {
				resource = root.getFile(path);
			}
			else {
				// This is a fallback for XML stored in a jar or zip files
				IEditorPart editor = SpringUIUtils.getActiveEditor();
				IEditorInput input = editor.getEditorInput();
				if (input instanceof IFileEditorInput) {
					resource = ((IFileEditorInput) input).getFile();
				}
				else if (input instanceof ZipEntryEditorInput) {
					resource = (IFile) ((ZipEntryEditorInput) input).getAdapter(IFile.class);
				}
				else if (input instanceof JarEntryEditorInput) {
					JarEntryFile jarFile = (JarEntryFile) ((JarEntryEditorInput) input).getStorage();
					IPackageFragmentRoot jarPackageFragmentRoot = jarFile.getPackageFragmentRoot();
					resource = jarPackageFragmentRoot.getJavaProject().getProject().getFile(".project");
				}
			}
		}
		return resource;
	}

	public static final Node getFirstReferenceableNodeById(Document document, String id, IFile file) {
		Map<String, Node> nodes = getReferenceableNodes(document, file);
		for (Entry<String, Node> node : nodes.entrySet()) {
			if (node.getKey().equals(id)) {
				return node.getValue();
			}
		}
		return null;
	}

	public static final List<Node> getReferenceableNodesById(Document document, String id, IFile file) {
		List<Node> nodes = new ArrayList<Node>();
		for (IReferenceableElementsLocator locator : NamespaceUtils.getAllElementsLocators()) {

			Map<String, Set<Node>> tempNodess = locator.getReferenceableElements(document, file);
			if (tempNodess != null) {
				for(String name: tempNodess.keySet()) {
					if (name.equals(id)) {
						Set<Node> tempNodes = tempNodess.get(name);
						for(Node tempNode: tempNodes) {
							updateList(tempNode, nodes);
						}
					}
				}
			}
		}
		return nodes;
	}
	
	/**
	 * Add newNode to nodes if it is not already in the list of nodes
	 * @param newNode
	 * @param nodes
	 */
	private static void updateList(Node newNode, List<Node> nodes) {
		IDOMNode domNewNode = (IDOMNode) newNode;
		boolean found = false;
		for(Node node: nodes) {
			if (newNode.getOwnerDocument().equals(node.getOwnerDocument())) {
				IDOMNode domNode = (IDOMNode) node;
				if (domNode.getStartOffset() == domNewNode.getStartOffset() && domNode.getEndOffset() == domNewNode.getEndOffset()) {
					found = true;
				}
			}
			
		}
		
		if (!found) {
			nodes.add(newNode);
		}
	}

	/**
	 * Returns the node from given document at specified offset.
	 * @param offset the offset with given document
	 * @return Node either element, doctype, text, or null
	 */
	public static final Node getNodeByOffset(IDocument document, int offset) {
		// get the node at offset (returns either: element, doctype, text)
		IndexedRegion inode = null;
		IStructuredModel sModel = null;
		try {
			sModel = org.eclipse.wst.sse.core.StructuredModelManager.getModelManager()
					.getExistingModelForRead(document);
			if (sModel == null && document instanceof IStructuredDocument) {
				sModel = org.eclipse.wst.sse.core.StructuredModelManager.getModelManager().getModelForRead(
						(IStructuredDocument) document);
			}
			inode = sModel.getIndexedRegion(offset);
			if (inode == null) {
				inode = sModel.getIndexedRegion(offset - 1);
			}
		}
		finally {
			if (sModel != null) {
				sModel.releaseFromRead();
			}
		}

		if (inode instanceof Node) {
			return (Node) inode;
		}
		return null;
	}

	/**
	 * Returns the non-blocking Progress Monitor form the StatuslineManger
	 * @return the progress monitor
	 */
	public static final IProgressMonitor getProgressMonitor() {
		IWorkbenchWindow activeWorkbenchWindow = Activator.getActiveWorkbenchWindow();

		// this check is to allow for non UI thread call to this method
		if (activeWorkbenchWindow == null) {
			return new NullProgressMonitor();
		}

		IEditorPart editor = activeWorkbenchWindow.getActivePage().getActiveEditor();
		if (editor != null && editor.getEditorSite() != null && editor.getEditorSite().getActionBars() != null
				&& editor.getEditorSite().getActionBars().getStatusLineManager() != null
				&& editor.getEditorSite().getActionBars().getStatusLineManager().getProgressMonitor() != null) {

			IStatusLineManager manager = editor.getEditorSite().getActionBars().getStatusLineManager();
			IProgressMonitor monitor = manager.getProgressMonitor();
			manager.setMessage("Processing completion proposals");
			manager.setCancelEnabled(true);
			return monitor;
		}
		else {
			return new NullProgressMonitor();
		}
	}

	public static final IProject getProject(IDocument document) {
		IProject project = null;
		IEditorInput input = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
				.getEditorInput();
		if (input instanceof ZipEntryEditorInput) {
			ZipEntryStorage storage = (ZipEntryStorage) ((ZipEntryEditorInput) input).getStorage();
			project = storage.getFile().getProject();
		}
		else {
			IFile file = BeansEditorUtils.getFile(document);
			project = file.getProject();
		}
		return project;
	}

	public static final Map<String, Node> getReferenceableNodes(Document document, IFile file) {
		Map<String, Node> nodes = new HashMap<String, Node>();
		for (IReferenceableElementsLocator locator : NamespaceUtils.getAllElementsLocators()) {

			Map<String, Set<Node>> tempNodes = locator.getReferenceableElements(document, file);
			if (tempNodes != null) {
				for(String name: tempNodes.keySet()) {
					Set<Node> set = tempNodes.get(name);
					if (set != null && set.size() > 0) {
						nodes.put(name, set.iterator().next());
					}
				}
			}
		}
		return nodes;
	}

	public static final boolean hasAttribute(Node node, String attributeName) {
		return (node != null && node.hasAttributes() && node.getAttributes().getNamedItem(attributeName) != null);
	}

	public static final boolean isSpringStyleOutline() {
		return Activator.getDefault().getPreferenceStore().getBoolean(IPreferencesConstants.OUTLINE_SPRING);
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

	/**
	 * Replace matching literal portions of a string with another string
	 */
	public static final String replace(String aString, String source, String target) {
		if (aString == null) {
			return null;
		}
		String normalString = ""; //$NON-NLS-1$
		int length = aString.length();
		int position = 0;
		int previous = 0;
		int spacer = source.length();
		while (position + spacer - 1 < length && aString.indexOf(source, position) > -1) {
			position = aString.indexOf(source, previous);
			normalString = normalString + aString.substring(previous, position) + target;
			position += spacer;
			previous = position;
		}
		normalString = normalString + aString.substring(position, aString.length());

		return normalString;
	}

	private static String resolveClassNameFromFactoryMethod(String factoryMethod, String className, IProject project) {
		IType type = JdtUtils.getJavaType(project, className);
		if (type != null) {
			try {
				Set<IMethod> methods = Introspector.getAllMethods(type);
				for (IMethod m : methods) {
					if (m.getElementName().equals(factoryMethod)) {
						return JdtUtils.resolveClassNameBySignature(m.getReturnType(), type);
					}
				}
			}
			catch (JavaModelException e) {
			}
		}
		return null;
	}

	public static String prepareMatchString(ContentAssistRequest request) {
		String matchString = request.getMatchString();
		return prepareMatchString(matchString);
	}

	public static String prepareMatchString(String matchString) {
		if (matchString == null) {
			matchString = "";
		}
		if (matchString.length() > 0 && (matchString.startsWith("\"") || matchString.startsWith("'"))) {
			matchString = matchString.substring(1);
		}
		return matchString;
	}
}
