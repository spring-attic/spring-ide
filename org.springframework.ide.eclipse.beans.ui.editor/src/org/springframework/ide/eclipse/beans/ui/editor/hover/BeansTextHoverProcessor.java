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
package org.springframework.ide.eclipse.beans.ui.editor.hover;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.taginfo.XMLTagInfoHoverProcessor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaDocUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Hover information processor to create hover information for the spring beans
 * editor
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansTextHoverProcessor extends XMLTagInfoHoverProcessor implements
		ITextHover {

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		String displayText = null;
		int documentOffset = hoverRegion.getOffset();
		displayText = computeHoverHelp(textViewer, documentOffset);
		if (displayText != null) {
			return displayText;
		}
		return super.getHoverInfo(textViewer, hoverRegion);
	}

	/**
	 * Retrieves documentation to display in the hover help popup.
	 * 
	 * @return String any documentation information to display <code>null</code>
	 * if there is nothing to display.
	 * 
	 */
	@Override
	protected String computeHoverHelp(ITextViewer textViewer,
			int documentPosition) {
		String result = null;

		IndexedRegion treeNode = BeansEditorUtils.getNodeAt(
				(StructuredTextViewer) textViewer, documentPosition);
		if (treeNode == null)
			return null;
		Node node = (Node) treeNode;

		while (node != null && node.getNodeType() == Node.TEXT_NODE
				&& node.getParentNode() != null)
			node = node.getParentNode();
		IDOMNode parentNode = (IDOMNode) node;

		IStructuredDocumentRegion flatNode = ((IStructuredDocument) textViewer
				.getDocument()).getRegionAtCharacterOffset(documentPosition);
		if (flatNode != null) {
			ITextRegion region = flatNode
					.getRegionAtCharacterOffset(documentPosition);
			if (region != null) {
				result = computeRegionHelp(treeNode, parentNode, flatNode,
						region, textViewer.getDocument());
			}
		}

		return result;
	}

	/**
	 * Computes the hoverhelp based on region
	 * 
	 * @return String hoverhelp
	 */
	protected String computeRegionHelp(IndexedRegion treeNode,
			IDOMNode parentNode, IStructuredDocumentRegion flatNode,
			ITextRegion region, IDocument document) {
		String result = null;
		if (region == null)
			return null;
		String regionType = region.getType();
		if (regionType == DOMRegionContext.XML_TAG_NAME)
			result = computeTagNameHelp((IDOMNode) treeNode, parentNode,
					flatNode, region);
		else if (regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME)
			result = computeTagAttNameHelp((IDOMNode) treeNode, parentNode,
					flatNode, region);
		else if (regionType == DOMRegionContext.XML_TAG_ATTRIBUTE_VALUE)
			result = computeTagAttValueHelp((IDOMNode) treeNode, parentNode,
					flatNode, region, document);
		return result;
	}

	/**
	 * Computes the hover help for the attribute value (this is the same as the
	 * attribute name's help)
	 */
	protected String computeTagAttValueHelp(IDOMNode xmlnode,
			IDOMNode parentNode, IStructuredDocumentRegion flatNode,
			ITextRegion region, IDocument document) {
		IFile file = BeansEditorUtils.getFile(document);

		ITextRegion attrNameRegion = getAttrNameRegion(xmlnode, region);
		String attName = flatNode.getText(attrNameRegion);
		NamedNodeMap attributes = xmlnode.getAttributes();
		String result = null;
		if ("class".equals(attName) && attributes.getNamedItem("class") != null) {
			String className = attributes.getNamedItem("class").getNodeValue();
			if (className != null) {
				IType type = JdtUtils.getJavaType(file.getProject(), className);
				if (type != null) {
					BeansJavaDocUtils utils = new BeansJavaDocUtils(type);
					result = utils.getJavaDoc();
				}
			}
		}
		else if ("name".equals(attName)
				&& "property".equals(xmlnode.getNodeName())) {

			String propertyName = attributes.getNamedItem(attName)
					.getNodeValue();
			String[] paths = StringUtils.split(propertyName, ".");
			if (paths == null) {
				paths = new String[] { propertyName };
			}
			List<String> propertyPaths = Arrays.asList(paths);
			List classNames = BeansEditorUtils.getClassNamesOfBean(file,
					xmlnode.getParentNode());
			List<IMethod> methods = new ArrayList<IMethod>();
			BeansEditorUtils.extractAllMethodsFromPropertyPathElements(
					propertyPaths, classNames, file, 0,
					methods);

			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < methods.size(); i++) {
				IMethod method = methods.get(i);

				if (method != null) {
					BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
					buf.append(utils.getJavaDoc() + "<br>");
				}
			}
			result = buf.toString();
		}
		else if ("local".equals(attName)
				&& attributes.getNamedItem(attName) != null) {
			Element ref = xmlnode.getOwnerDocument().getElementById(
					attributes.getNamedItem(attName).getNodeValue());
			result = BeansEditorUtils.createAdditionalProposalInfo(ref, file);
		}
		else if ("bean".equals(attName)
				&& attributes.getNamedItem(attName) != null) {
			String target = attributes.getNamedItem(attName).getNodeValue();
			// assume this is an external reference
			Iterator beans = BeansEditorUtils.getBeansFromConfigSets(file)
					.iterator();
			while (beans.hasNext()) {
				IBean modelBean = (IBean) beans.next();
				if (modelBean.getElementName().equals(target)) {
					result = BeansEditorUtils
							.createAdditionalProposalInfo(modelBean);
				}
			}
		}
		else if (("ref".equals(attName)
				|| "local".equals(attName)
				|| "parent".equals(attName)
				|| "depends-on".equals(attName)
				|| "factory-bean".equals(attName)
				|| "key-ref".equals(attName)
				|| "value-ref".equals(attName)
				|| attName.endsWith("-ref")
				|| ("name".equals(attName) && "alias".equals(xmlnode
						.getNodeName())) || ("bean".equals(attName) && "ref"
				.equals(xmlnode.getNodeName())))
				&& attributes.getNamedItem(attName) != null) {
			Element ref = xmlnode.getOwnerDocument().getElementById(
					attributes.getNamedItem(attName).getNodeValue());
			if (ref != null) {
				result = BeansEditorUtils.createAdditionalProposalInfo(ref,
						file);
			}
			else {
				String target = attributes.getNamedItem(attName).getNodeValue();
				// assume this is an external reference
				Iterator beans = BeansEditorUtils.getBeansFromConfigSets(file)
						.iterator();
				while (beans.hasNext()) {
					IBean modelBean = (IBean) beans.next();
					if (modelBean.getElementName().equals(target)) {
						result = BeansEditorUtils
								.createAdditionalProposalInfo(modelBean);
					}
				}
			}
		}
		else if (("factory-method").equals(attName)) {
			String factoryMethod = attributes.getNamedItem(attName)
					.getNodeValue();
			String className = BeansEditorUtils.getClassNameForBean(
					file, xmlnode.getOwnerDocument(), xmlnode);
			IType type = JdtUtils.getJavaType(file.getProject(), className);
			if (type != null) {
				try {
					IMethod[] methods = type.getMethods();
					for (IMethod method : methods) {
						if (method.getElementName().equals(factoryMethod)) {
							BeansJavaDocUtils utils = new BeansJavaDocUtils(
									method);
							result = utils.getJavaDoc();
						}
					}
				}
				catch (JavaModelException e) {
				}
			}

		}
		if ("init-method".equals(attName) || "destroy-method".equals(attName)) {
			String factoryMethod = attributes.getNamedItem(attName)
					.getNodeValue();
			String className = BeansEditorUtils.getClassNameForBean(
					file, xmlnode.getOwnerDocument(), xmlnode);
			IType type = JdtUtils.getJavaType(file.getProject(), className);
			try {
				IMethod method = Introspector.findMethod(type, factoryMethod,
						0, Public.DONT_CARE, Static.DONT_CARE);
				if (method != null) {
					BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
					result = utils.getJavaDoc();
				}
			}
			catch (JavaModelException e) {
			}
		}
		else if (("lookup-method".equals(xmlnode.getNodeName()) && "name"
				.equals(attName))
				|| ("replaced-method".equals(xmlnode.getNodeName()) && "name"
						.equals(attName))) {
			String factoryMethod = attributes.getNamedItem(attName)
					.getNodeValue();
			String className = BeansEditorUtils.getClassNameForBean(
					file, xmlnode.getOwnerDocument(), 
					xmlnode.getParentNode());
			IType type = JdtUtils.getJavaType(file.getProject(), className);
			try {
				IMethod method = Introspector.findMethod(type, factoryMethod,
						0, Public.DONT_CARE, Static.DONT_CARE);
				if (method != null) {
					BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
					result = utils.getJavaDoc();
				}
			}
			catch (JavaModelException e) {
			}
		}
		if (result != null && !"".equals(result)) {
			return result;
		}
		else {
			return super.computeTagAttValueHelp(xmlnode, parentNode, flatNode,
					attrNameRegion);
		}
	}
}
