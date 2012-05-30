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
package org.springframework.ide.eclipse.wizard.ui;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalAdapter;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.ContentAssistProposalWrapper;
import org.springframework.ide.eclipse.wizard.Messages;
import org.springframework.ide.eclipse.wizard.core.WizardBeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.wizard.core.WizardContentAssistConverter;


/**
 * Dialog for editing constructor arg of a bean.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ConstructorArgDialog extends BeanChildDialog {

	private static final String DEFAULT_MESSAGE = Messages.getString("ConstructorArgDialog.DEFAULT_MESSAGE"); //$NON-NLS-1$

	private String refErrorMessage, valueErrorMessage;

	private Text refText, valueText;

	private String originalRef, originalValue;

	protected ConstructorArgDialog(Shell parentShell, BeanWizard wizard, IDOMElement constructorArg, String title,
			boolean isNew) {
		super(parentShell, wizard, constructorArg, title, isNew);

		if (constructorArg.hasAttribute(BeansSchemaConstants.ATTR_REF)) {
			this.originalRef = constructorArg.getAttribute(BeansSchemaConstants.ATTR_REF);
		}
		if (constructorArg.hasAttribute(BeansSchemaConstants.ATTR_VALUE)) {
			this.originalValue = constructorArg.getAttribute(BeansSchemaConstants.ATTR_VALUE);
		}
	}

	private void checkValueAndRef() {
		if (valueText.getText().length() > 0 && refText.getText().length() > 0) {
			valueErrorMessage = Messages.getString("ElementDialog.VALUE_REF_OVERLAP_MESSAGE"); //$NON-NLS-1$
		}
		else {
			valueErrorMessage = null;
		}
	}

	private void createAttribute(String attributeName, Label label, Text text) {
		label.setText(attributeName + ":"); //$NON-NLS-1$
		label.setLayoutData(new GridData());

		text.setEditable(true);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		XmlBackedContentProposalProvider proposalProvider = null;

		IDOMDocument originalDocument = wizard.getOriginalDocument();
		IFile beanFile = wizard.getBeanFile();
		if (attributeName.equals(BeansSchemaConstants.ATTR_REF)) {
			proposalProvider = new WizardBeanReferenceContentProposalProvider(element, attributeName, beanFile,
					originalDocument);
		}

		if (proposalProvider != null) {
			if (attributeName.equals(BeansSchemaConstants.ATTR_NAME)) {
				new XmlBackedContentProposalAdapter(text, new TextContentAdapter(), proposalProvider, null);
			}
			new XmlBackedContentProposalAdapter(text, new TextContentAdapter(), proposalProvider);
		}

		addListener(text, attributeName);
	}

	@Override
	protected void createAttributes(Composite container) {
		Label valueLabel = new Label(container, SWT.NONE);
		valueText = new Text(container, SWT.BORDER);
		if (originalValue != null) {
			valueText.setText(originalValue);
		}
		createAttribute(BeansSchemaConstants.ATTR_VALUE, valueLabel, valueText);

		Label refLabel = new Label(container, SWT.NONE);
		refText = new Text(container, SWT.BORDER);
		if (originalRef != null) {
			refText.setText(originalRef);
		}
		createAttribute(BeansSchemaConstants.ATTR_REF, refLabel, refText);
	}

	@Override
	protected boolean getCanFinish() {
		return valueErrorMessage == null;
	}

	@Override
	protected String getDefaultMessage() {
		return DEFAULT_MESSAGE;
	}

	@Override
	protected String getMessage() {
		if (valueErrorMessage != null) {
			return valueErrorMessage;
		}

		if (refErrorMessage != null) {
			return refErrorMessage;
		}

		return null;
	}

	@Override
	protected void resetAttributes() {
		resetAttribute(BeansSchemaConstants.ATTR_REF, originalRef);
		resetAttribute(BeansSchemaConstants.ATTR_VALUE, originalValue);
	}

	@Override
	protected void validateAttribute(String attributeName, String value,
			WizardContentAssistConverter contentAssistConverter) {
		Set<ContentAssistProposalWrapper> proposals = null;

		if (attributeName.equals(BeansSchemaConstants.ATTR_REF)) {
			checkValueAndRef();
			if (value.length() > 0) {
				proposals = contentAssistConverter.getReferenceableBeanDescriptions(value, true);
				if (proposals.isEmpty()) {
					refErrorMessage = Messages.getString("ElementDialog.UNKNOWN_REF_MESSAGE"); //$NON-NLS-1$
				}
				else {
					refErrorMessage = null;
				}
			}
			else {
				refErrorMessage = null;
			}
		}
		else if (attributeName.equals(BeansSchemaConstants.ATTR_VALUE)) {
			checkValueAndRef();
		}

		updateMessage();
	}

	@Override
	protected void validateAttributes() {
		validateAttribute(BeansSchemaConstants.ATTR_REF, refText.getText());
	}

}
