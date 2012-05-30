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
package org.springframework.ide.eclipse.config.ui.editors.context;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.providers.BeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ClassHierarchyContentProposalProvider;
import org.springframework.ide.eclipse.config.core.contentassist.providers.PackageContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.ContextSchemaConstants;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ContextDetailsSectionPart extends SpringConfigDetailsSectionPart {

	public ContextDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
	}

	@Override
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		// ContextContentAssistProcessor and ContextHyperlinkDetector
		String elem = getInput().getLocalName();
		if (ContextSchemaConstants.ELEM_COMPONENT_SCAN.equals(elem)
				&& (ContextSchemaConstants.ATTR_NAME_GENERATOR.equals(attr) || ContextSchemaConstants.ATTR_SCOPE_RESOLVER
						.equals(attr))) {
			TextAttribute attrControl = createBeanAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new BeanReferenceContentProposalProvider(
					getInput(), attr, true)));
			return true;
		}
		if (ContextSchemaConstants.ELEM_LOAD_TIME_WEAVER.equals(elem)
				&& ContextSchemaConstants.ATTR_WEAVER_CLASS.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, true, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassHierarchyContentProposalProvider(
					getInput(), attr, "org.springframework.instrument.classloading.LoadTimeWeaver"))); //$NON-NLS-1$
			return true;
		}
		if (ContextSchemaConstants.ELEM_COMPONENT_SCAN.equals(elem)
				&& ContextSchemaConstants.ATTR_BASE_PACKAGE.equals(attr)) {
			TextAttribute attrControl = createTextAttribute(client, attr, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new PackageContentProposalProvider(getInput(),
					attr)));
			return true;
		}
		if ((ContextSchemaConstants.ELEM_INCLUDE_FILTER.equals(elem) || ContextSchemaConstants.ELEM_EXCLUDE_FILTER
				.equals(elem)) && ContextSchemaConstants.ATTR_EXPRESSION.equals(attr)) {
			TextAttribute attrControl = createClassAttribute(client, attr, false, required);
			addWidget(attrControl);
			addAdapter(new TextAttributeProposalAdapter(attrControl, new ClassContentProposalProvider(getInput(), attr,
					false)));
			return true;
		}
		return super.addCustomAttribute(client, attr, required);
	}

}
