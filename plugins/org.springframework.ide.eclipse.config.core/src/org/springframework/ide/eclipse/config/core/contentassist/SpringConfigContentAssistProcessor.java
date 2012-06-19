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
package org.springframework.ide.eclipse.config.core.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQueryAction;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class SpringConfigContentAssistProcessor extends XMLContentAssistProcessor {

	public SpringConfigContentAssistProcessor() {
		super();
	}

	public boolean allowsCharacterData(IDOMElement element) {
		if (element != null) {
			CMElementDeclaration elementDecl = getCMElementDeclaration(element);
			if (elementDecl != null) {
				return elementDecl.getContentType() == CMElementDeclaration.MIXED
						|| elementDecl.getContentType() == CMElementDeclaration.CDATA
						|| elementDecl.getContentType() == CMElementDeclaration.PCDATA;
			}
		}
		return false;
	}

	public List<CMAttributeDeclaration> getAttributeDeclarations(IDOMElement element) {
		List<CMAttributeDeclaration> decls = new ArrayList<CMAttributeDeclaration>();
		if (element != null) {
			CMElementDeclaration elementDecl = getCMElementDeclaration(element);
			if (elementDecl != null) {
				List cmnodes = ModelQueryUtil.getModelQuery(element.getOwnerDocument()).getAvailableContent(element,
						elementDecl, ModelQuery.INCLUDE_ATTRIBUTES);
				Iterator nodeIterator = cmnodes.iterator();
				while (nodeIterator.hasNext()) {
					Object o = nodeIterator.next();
					if (o instanceof CMAttributeDeclaration) {
						CMAttributeDeclaration cmnode = (CMAttributeDeclaration) o;
						decls.add(cmnode);
					}
				}
			}
		}
		return decls;
	}

	/**
	 * Looks up the schema declaration for the given element and returns an
	 * alphabetically sorted list of the possible attribute names.
	 * 
	 * @param element the parent element
	 * @return sorted list of attribute names for the given element
	 */
	public List<String> getAttributeNames(IDOMElement element) {
		List<CMAttributeDeclaration> decls = getAttributeDeclarations(element);
		List<String> attrs = new ArrayList<String>();
		for (CMAttributeDeclaration decl : decls) {
			attrs.add(decl.getAttrName());
		}
		Collections.sort(attrs);
		return attrs;
	}

	/**
	 * Looks up the schema declaration for the given element and returns an
	 * alphabetically sorted list of the possible child names. Note that this
	 * method can incorrectly return empty if invoked very early in the editor
	 * life cycle.
	 * 
	 * @param element the parent element
	 * @return sorted list of child names for the given parent
	 */
	public List<String> getChildNames(IDOMElement element) {
		List<String> children = new ArrayList<String>();
		if (element != null) {
			List cmnodes = getAvailableChildElementDeclarations(element, 0, ModelQueryAction.INSERT);
			Iterator nodeIterator = cmnodes.iterator();
			while (nodeIterator.hasNext()) {
				Object o = nodeIterator.next();
				if (o instanceof CMElementDeclaration) {
					CMElementDeclaration elementDecl = (CMElementDeclaration) o;
					String tagName = getRequiredName(element, elementDecl);
					children.add(tagName);
				}
			}
		}
		Collections.sort(children);
		return children;
	}

	/**
	 * Looks up the schema declaration for the given element and returns any
	 * documentation information.
	 * 
	 * @param element
	 * @return documentation information for the given element
	 */
	public String getDocumentation(IDOMElement element) {
		if (element != null) {
			CMElementDeclaration elementDecl = getCMElementDeclaration(element);
			if (elementDecl != null) {
				String documentation = ConfigCoreUtils.stripTags(getAdditionalInfo(
						getCMElementDeclaration(element.getParentNode()), elementDecl));
				if (documentation != null) {
					return documentation;
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	public void insertDefaultAttributes(IDOMElement element) {
		List<CMAttributeDeclaration> decls = getAttributeDeclarations(element);
		for (CMAttributeDeclaration decl : decls) {
			if (decl.getUsage() == CMAttributeDeclaration.REQUIRED) {
				String attrName = decl.getAttrName();
				CMDataType attrType = decl.getAttrType();
				String attrValue = element.getAttribute(attrName);
				// We don't want to overwrite an existing value.
				if (attrValue == null || attrValue.trim().length() == 0) {
					if (attrType != null) {
						if (attrType.getImpliedValueKind() != CMDataType.IMPLIED_VALUE_NONE
								&& attrType.getImpliedValue() != null) {
							attrValue = attrType.getImpliedValue();
						}
						else if (attrType.getEnumeratedValues() != null && attrType.getEnumeratedValues().length > 0) {
							attrValue = attrType.getEnumeratedValues()[0];
						}
					}
					element.setAttribute(attrName, attrValue);
				}
			}
		}
	}

	public boolean isRequiredAttribute(CMAttributeDeclaration attrDecl) {
		if (attrDecl != null) {
			return attrDecl.getUsage() == CMAttributeDeclaration.REQUIRED;
		}
		return false;
	}

}
