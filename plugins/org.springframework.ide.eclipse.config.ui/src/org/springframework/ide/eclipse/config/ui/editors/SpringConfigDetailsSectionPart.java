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
package org.springframework.ide.eclipse.config.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalAdapter;
import org.springframework.ide.eclipse.config.core.contentassist.providers.ToolAnnotationContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.ui.widgets.AbstractAttributeWidget;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttribute;
import org.springframework.ide.eclipse.config.ui.widgets.TextAttributeProposalAdapter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class SpringConfigDetailsSectionPart extends AbstractConfigDetailsSectionPart {

	private class CMAttributeDeclarationComparator implements Comparator<CMAttributeDeclaration> {
		public int compare(CMAttributeDeclaration o1, CMAttributeDeclaration o2) {
			String name1 = o1.getNodeName();
			String name2 = o2.getNodeName();
			boolean required1 = processor.isRequiredAttribute(o1);
			boolean required2 = processor.isRequiredAttribute(o2);

			// Sort:
			// 1. ID attributes at the top
			// 2. followed by required attributes in alphabetical order
			// 3. followed by other attributes in alphabetical order
			if (name1.equalsIgnoreCase(BeansSchemaConstants.ATTR_ID)) {
				return -1;
			}
			if (name2.equalsIgnoreCase(BeansSchemaConstants.ATTR_ID)) {
				return 1;
			}
			if (required1 && required2) {
				return name1.compareToIgnoreCase(name2);
			}
			if (required1) {
				return -1;
			}
			if (required2) {
				return 1;
			}
			return name1.compareToIgnoreCase(name2);
		}
	}

	private final SpringConfigContentAssistProcessor processor;

	private final List<AbstractAttributeWidget> widgets;

	private final List<XmlBackedContentProposalAdapter> adapters;

	private List<CMAttributeDeclaration> attrDecls;

	public SpringConfigDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit) {
		super(editor, input, parent, toolkit);
		processor = editor.getXmlProcessor();
		widgets = new ArrayList<AbstractAttributeWidget>();
		adapters = new ArrayList<XmlBackedContentProposalAdapter>();
		attrDecls = processor.getAttributeDeclarations(input);
	}

	protected void addAdapter(XmlBackedContentProposalAdapter adapter) {
		adapters.add(adapter);
	}

	private void addComboAttribute(Composite client, String attr, String[] enumStrs, boolean required) {
		boolean hasEmptyStr = false;
		String[] enumStrsCopy = new String[enumStrs.length + 1];
		enumStrsCopy[0] = ""; //$NON-NLS-1$
		for (int i = 0; i < enumStrs.length; i++) {
			enumStrsCopy[i + 1] = enumStrs[i];
			if (enumStrs[0].trim().length() == 0) {
				hasEmptyStr = true;
			}
		}
		if (!hasEmptyStr) {
			enumStrs = enumStrsCopy;
		}
		widgets.add(createComboAttribute(client, attr, enumStrs, required));
	}

	/**
	 * This method is called automatically when the details section is created.
	 * Clients may override to add their own custom widgets with hyperlinks and
	 * content proposals. Must return true if a widget was created, and false
	 * otherwise so that the page can create a plain text field for the
	 * attribute.
	 * 
	 * @param client the parent composite
	 * @param attr the attribute name
	 * @param required denotes whether this is a required field
	 * @return true if an attribute widget was created, false otherwise
	 */
	protected boolean addCustomAttribute(Composite client, String attr, boolean required) {
		return false;
	}

	private void addTextAttribute(Composite client, String attr, boolean required) {
		boolean widgetCreated = false;
		List<Element> appInfo = ToolAnnotationUtils.getApplicationInformationElements(getInput(), attr);
		if (!appInfo.isEmpty()) {
			for (Element element : appInfo) {
				NodeList children = element.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node annotation = children.item(i);
					ToolAnnotationData data = ToolAnnotationUtils.getToolAnnotationData(annotation);
					if (!widgetCreated) {
						TextAttribute attrWidget = null;
						if (data.getExpectedType() != null
								&& data.getExpectedType().equalsIgnoreCase(Class.class.getName())) {
							attrWidget = createClassAttribute(client, attr, true, required);
						}
						else if (data.getKind() != null || data.getExpectedMethodType() != null
								|| data.getExpectedMethodRef() != null || data.getExpectedMethodExpression() != null) {
							attrWidget = createToolAnnotationAttribute(client, attr, required);
						}
						if (attrWidget != null) {
							widgets.add(attrWidget);
							adapters.add(new TextAttributeProposalAdapter(attrWidget,
									new ToolAnnotationContentProposalProvider(getInput(), attr)));
							widgetCreated = true;
						}
					}
				}
			}
		}
		if (!widgetCreated) {
			if (!addCustomAttribute(client, attr.toLowerCase(), required)) {
				widgets.add(createTextAttribute(client, attr, required));
			}
		}
	}

	protected void addWidget(AbstractAttributeWidget widget) {
		widgets.add(widget);
	}

	@Override
	protected void createAttributes(Composite client) {
		if (attrDecls.isEmpty() && processor.allowsCharacterData(getInput())) {
			widgets.add(createTextArea(client, getInput().getLocalName()));
		}
		else {
			Collections.sort(attrDecls, new CMAttributeDeclarationComparator());
			for (CMAttributeDeclaration attrDecl : attrDecls) {
				boolean required = processor.isRequiredAttribute(attrDecl);
				String attr = attrDecl.getNodeName();
				String[] enumStrs = attrDecl.getAttrType().getEnumeratedValues();
				if (enumStrs != null && enumStrs.length > 0) {
					addComboAttribute(client, attr, enumStrs, required);
				}
				else {
					addTextAttribute(client, attr, required);
				}
			}
		}
	}

	@Override
	public void refresh() {
		for (AbstractAttributeWidget widget : widgets) {
			widget.update();
		}
		for (XmlBackedContentProposalAdapter adapter : adapters) {
			adapter.update(getInput());
		}
		super.refresh();
	}

	@Override
	public boolean setFormInput(Object input) {
		boolean result = super.setFormInput(input);
		attrDecls = processor.getAttributeDeclarations(getInput());
		return result;
	}

}
