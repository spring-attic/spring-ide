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
package org.springframework.ide.eclipse.config.ui.editors.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassHierarchyContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.FieldContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.UtilSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class UtilDetailsSectionPart extends SpringConfigDetailsSectionPart {

	public UtilDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent, FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
	}

	@Override
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		// UtilContentAssistProcessor and UtilHyperlinkDetector
		String elem = getInput().getLocalName();
		if (UtilSchemaConstants.ATTR_LIST_CLASS.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, true, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassHierarchyContentProposalProvider(
					getInput(), attr, List.class.getName())));
			return true;
		}
		if (UtilSchemaConstants.ATTR_MAP_CLASS.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, true, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassHierarchyContentProposalProvider(
					getInput(), attr, Map.class.getName())));
			return true;
		}
		if (UtilSchemaConstants.ATTR_SET_CLASS.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, true, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassHierarchyContentProposalProvider(
					getInput(), attr, Set.class.getName())));
			return true;
		}
		if (UtilSchemaConstants.ATTR_VALUE_TYPE.equals(attr) || UtilSchemaConstants.ATTR_KEY_TYPE.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, false, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassContentProposalProvider(getInput(), attr,
					false)));
			return true;
		}
		if (UtilSchemaConstants.ELEM_CONSTANT.equals(elem) && UtilSchemaConstants.ATTR_STATIC_FIELD.equals(attr)) {
			TextAttribute attrControl = createFieldAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new FieldContentProposalProvider(getInput(), attr)));
			return true;
		}
		return super.addCustomAttribute(client, attr, required);
	}

}
