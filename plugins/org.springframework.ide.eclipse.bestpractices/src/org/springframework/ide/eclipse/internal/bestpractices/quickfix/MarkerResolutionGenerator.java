/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.FactoryMethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.InitDestroyMethodContentAssistCalculator;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.processors.BeanReferenceQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.ClassAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.ConstructorArgQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.MethodAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.PropertyAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.RequiredPropertyQuickAssistProcessor;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Produces an IMarkerResolution instance for a given marker, if applicable.
 * This Generator currently supports only one resolution per rule.
 * @author Wesley Coelho
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Terry Denney
 */
public class MarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	private class NodeInfo {
		private final IDOMNode node;

		private final int offset;

		private final int length;

		private NodeInfo(IDOMNode node, int offset, int length) {
			this.node = node;
			this.offset = offset;
			this.length = length;
		}
	}

	private static final String ERROR_ID_CLASS_NOT_FOUND = "CLASS_NOT_FOUND";

	private static final String ERROR_ID_UNDEFINED_REFERENCED_BEAN = "UNDEFINED_REFERENCED_BEAN";

	private static final String ERROR_ID_NO_SETTER = "NO_SETTER";

	private static final String ERROR_ID_NO_GETTER = "NO_GETTER";

	private static final String ERROR_ID_KEY = "errorId";

	private static final String ERROR_ID_UNDEFINED_INIT = "UNDEFINED_INIT_METHOD";

	private static final String ERROR_ID_UNDEFINED_DESTROY = "UNDEFINED_DESTROY_METHOD";

	private static final String ERROR_ID_REQUIRED_PROPERTY_MISSING = "REQUIRED_PROPERTY_MISSING";

	private static final String ERROR_ID_NO_CONSTRUCTOR = "NO_CONSTRUCTOR";

	private static final String ERROR_ID_UNDEFINED_FACTORY_BEAN_METHOD = "UNDEFINED_FACTORY_BEAN_METHOD";

	private IDOMNode findMatchedBean(NodeList nodes, String beanName) {
		if (nodes == null) {
			return null;
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node item = nodes.item(i);
			if (BeansSchemaConstants.ELEM_BEAN.equals(item.getNodeName())) {
				if (item.getAttributes() != null) {
					AttrImpl attr = (AttrImpl) item.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_NAME);
					if (attr != null) {
						if (beanName.equals(attr.getNodeValue())) {
							return (IDOMNode) item;
						}
					}

					attr = (AttrImpl) item.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_ID);
					if (attr != null) {
						if (beanName.equals(attr.getNodeValue())) {
							return (IDOMNode) item;
						}
					}
				}
			}

			IDOMNode childNode = findMatchedBean(item.getChildNodes(), beanName);
			if (childNode != null) {
				return childNode;
			}
		}

		return null;
	}

	private NodeInfo findNodeInfo(IDOMNode node, String attrName, String attrValue) {
		if (node != null) {
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					AttrImpl attribute = (AttrImpl) attributes.item(i);
					if (attrName == null || attrName.equals(attribute.getNodeName())) {
						if (attrValue != null && attrValue.equals(attribute.getNodeValue())) {
							int offset = attribute.getValueRegionStartOffset();

							// increase offset if value starts with
							// "
							if (attribute.getValueRegion().getLength() > attrValue.length()) {
								offset++;
							}
							int length = attribute.getNodeValue() == null ? 0 : attribute.getNodeValue().length();
							return new NodeInfo(node, offset, length);
						}
					}
				}
			}

			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				NodeInfo info = findNodeInfo((IDOMNode) children.item(i), attrName, attrValue);
				if (info != null) {
					return info;
				}
			}
		}

		return null;
	}

	protected NodeInfo findNodeInfo(IMarker marker, String attrName, String attrValue) {
		IDOMModel model = null;
		try {
			String beanName = (String) marker.getAttribute("BEAN_NAME");

			if (beanName == null) {
				return null;
			}

			IFile file = (IFile) marker.getResource();
			model = (IDOMModel) StructuredModelManager.getModelManager().getModelForRead(file);
			if (model != null) {
				IDOMDocument document = model.getDocument();
				if (document != null) {
					NodeList nodes = document.getChildNodes();
					IDOMNode node = findMatchedBean(nodes, beanName);
					return findNodeInfo(node, attrName, attrValue);
				}
			}
		}
		catch (CoreException e) {

		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	/**
	 * Return the resolution(s) for the given marker. Currently supports only
	 * one resolution per marker.
	 */
	public IMarkerResolution[] getResolutions(IMarker marker) {
		ICompletionProposal[] proposals = null;

		// Do not offer resolutions if the editor is dirty because the marker
		// position may be incorrect
		IEditorPart editor = XmlQuickFixUtil.getMarkedEditor(marker);
		if (editor != null && editor.isDirty()) {
			return new IMarkerResolution[0];
		}

		try {
			String errorId = (String) marker.getAttribute(ERROR_ID_KEY);
			String className = marker.getAttribute("CLASS", null);
			IFile file = marker.getResource() instanceof IFile ? (IFile) marker.getResource() : null;

			if (ERROR_ID_NO_CONSTRUCTOR.equals(errorId)) {
				int numArgument = marker.getAttribute("NUM_ARGUMENT", 0);
				if (className != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_CLASS, className);
					if (nodeInfo != null && file != null) {

						List<String> argClassNames = new ArrayList<String>();
						for (int i = 0; i < numArgument; i++) {
							// TODO: narrow this down to a type
							argClassNames.add("Object");
						}
						proposals = new ConstructorArgQuickAssistProcessor(nodeInfo.offset, nodeInfo.length, className,
								file.getProject(), false, argClassNames, nodeInfo.node)
								.computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_REQUIRED_PROPERTY_MISSING.equals(errorId)) {
				List<String> requiredProperties = new ArrayList<String>();
				String requiredProperty = null;
				int counter = 1;
				do {
					requiredProperty = marker.getAttribute("MISSING_PROPERTIES" + counter, null);
					if (requiredProperty != null) {
						requiredProperties.add(requiredProperty);
					}
					counter++;
				} while (requiredProperty != null);

				if (requiredProperties.size() > 0 && className != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_CLASS, className);
					if (nodeInfo != null) {
						proposals = new RequiredPropertyQuickAssistProcessor(nodeInfo.offset, nodeInfo.length,
								className, false, requiredProperties, nodeInfo.node).computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_UNDEFINED_DESTROY.equals(errorId)) {
				String methodName = marker.getAttribute("METHOD", null);
				if (methodName != null && className != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_DESTROY_METHOD, methodName);
					if (nodeInfo != null && file != null) {
						proposals = new MethodAttributeQuickAssistProcessor(nodeInfo.offset, nodeInfo.length,
								className, methodName, true, nodeInfo.node, BeansSchemaConstants.ATTR_DESTROY_METHOD,
								file.getProject(), false, new InitDestroyMethodContentAssistCalculator(), file)
								.computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_UNDEFINED_INIT.equals(errorId)) {
				String methodName = marker.getAttribute("METHOD", null);
				if (methodName != null && className != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_INIT_METHOD, methodName);
					if (nodeInfo != null && file != null) {
						proposals = new MethodAttributeQuickAssistProcessor(nodeInfo.offset, nodeInfo.length,
								className, methodName, true, nodeInfo.node, BeansSchemaConstants.ATTR_INIT_METHOD,
								file.getProject(), false, new InitDestroyMethodContentAssistCalculator(), file)
								.computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_UNDEFINED_FACTORY_BEAN_METHOD.equals(errorId)) {
				String methodName = marker.getAttribute("METHOD", null);
				if (methodName != null && className != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_FACTORY_METHOD, methodName);
					if (nodeInfo != null && file != null) {
						proposals = new MethodAttributeQuickAssistProcessor(nodeInfo.offset, nodeInfo.length,
								className, methodName, true, nodeInfo.node, BeansSchemaConstants.ATTR_FACTORY_METHOD,
								file.getProject(), false, new FactoryMethodContentAssistCalculator(), file)
								.computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_NO_SETTER.equals(errorId)) {
				String propertyName = marker.getAttribute("PROPERTY", null);
				if (className != null && propertyName != null && file != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_NAME, propertyName);
					if (nodeInfo != null) {
						proposals = new PropertyAttributeQuickAssistProcessor(nodeInfo.offset, nodeInfo.length,
								className, propertyName, file.getProject(), false,
								PropertyAttributeQuickAssistProcessor.Type.SETTER).computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_NO_GETTER.equals(errorId)) {
				String propertyName = marker.getAttribute("PROPERTY", null);
				if (className != null && propertyName != null && file != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_NAME, propertyName);
					if (nodeInfo != null) {
						proposals = new PropertyAttributeQuickAssistProcessor(nodeInfo.offset, nodeInfo.length,
								className, propertyName, file.getProject(), false,
								PropertyAttributeQuickAssistProcessor.Type.GETTER).computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_CLASS_NOT_FOUND.equals(errorId)) {
				if (className != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_CLASS, className);
					if (nodeInfo != null && file != null) {
						proposals = new ClassAttributeQuickAssistProcessor(nodeInfo.offset, nodeInfo.length, className,
								file.getProject(), false, new HashSet<String>(), 0).computeQuickAssistProposals(null);
					}
				}
			}
			else if (ERROR_ID_UNDEFINED_REFERENCED_BEAN.equals(errorId)) {
				String beanName = marker.getAttribute("BEAN", null);
				if (beanName != null) {
					NodeInfo nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_REF, beanName);
					if (nodeInfo == null) {
						nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_PARENT, beanName);
					}
					if (nodeInfo == null) {
						nodeInfo = findNodeInfo(marker, BeansSchemaConstants.ATTR_FACTORY_BEAN, beanName);
					}
					if (nodeInfo != null && file != null) {
						proposals = new BeanReferenceQuickAssistProcessor(nodeInfo.offset, nodeInfo.length, beanName,
								false, nodeInfo.node, BeansSchemaConstants.ATTR_REF, beanName, file)
								.computeQuickAssistProposals(null);
					}
				}
			}
		}
		catch (CoreException e) {
			StatusHandler.log(e.getStatus());
		}

		if (proposals != null) {
			List<IMarkerResolution> resolutions = new ArrayList<IMarkerResolution>();
			for (ICompletionProposal proposal : proposals) {
				if (proposal instanceof IMarkerResolution) {
					resolutions.add((IMarkerResolution) proposal);
				}
			}
			return resolutions.toArray(new IMarkerResolution[resolutions.size()]);
		}
		return new IMarkerResolution[0];
	}

	public boolean hasResolutions(IMarker marker) {
		String errorId = marker.getAttribute(ERROR_ID_KEY, null);
		String className = marker.getAttribute("CLASS", null);

		if (errorId == null) {
			return false;
		}

		if (ERROR_ID_NO_CONSTRUCTOR.equals(errorId)) {
			return className != null;
		}
		else if (ERROR_ID_REQUIRED_PROPERTY_MISSING.equals(errorId)) {
			return true;
		}
		else if (ERROR_ID_UNDEFINED_DESTROY.equals(errorId)) {
			String methodName = marker.getAttribute("METHOD", null);
			return methodName != null && className != null;
		}
		else if (ERROR_ID_UNDEFINED_INIT.equals(errorId)) {
			String methodName = marker.getAttribute("METHOD", null);
			return methodName != null && className != null;
		}
		else if (ERROR_ID_UNDEFINED_FACTORY_BEAN_METHOD.equals(errorId)) {
			String methodName = marker.getAttribute("METHOD", null);
			return methodName != null && className != null;
		}
		else if (ERROR_ID_NO_SETTER.equals(errorId)) {
			String propertyName = marker.getAttribute("PROPERTY", null);
			return propertyName != null && className != null;
		}
		else if (ERROR_ID_NO_GETTER.equals(errorId)) {
			String propertyName = marker.getAttribute("PROPERTY", null);
			return propertyName != null && className != null;
		}
		else if (ERROR_ID_CLASS_NOT_FOUND.equals(errorId)) {
			return className != null;
		}
		else if (ERROR_ID_UNDEFINED_REFERENCED_BEAN.equals(errorId)) {
			String beanName = marker.getAttribute("BEAN", null);
			return beanName != null;
		}

		return false;
	}

}
