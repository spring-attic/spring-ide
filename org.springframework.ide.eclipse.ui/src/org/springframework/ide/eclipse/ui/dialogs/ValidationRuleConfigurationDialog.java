/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.dialogs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.util.StringUtils;

/**
 * {@link Dialog} to configure property values for {@link IValidationRule}.
 * @author Christian Dupuis
 * @since 2.0.4
 */
public class ValidationRuleConfigurationDialog extends TrayDialog {

	private final Map<String, String> propertyValues;

	private final Map<String, Integer> messageSeverities;

	private final String ruleName;

	private final ValidationRuleDefinition validationRule;

	private Label errorLabel;

	private Button okButton;

	private Set<PropertyModifyListener> propertyModifyListeners = new HashSet<PropertyModifyListener>();

	private Set<SeverityModifyListener> severityModifyListeners = new HashSet<SeverityModifyListener>();

	public ValidationRuleConfigurationDialog(Shell parentShell, Map<String, String> propertyValues,
			Map<String, Integer> messageSeverities, ValidationRuleDefinition ruleDefinition) {
		super(parentShell);
		this.validationRule = ruleDefinition;
		this.propertyValues = propertyValues;
		this.messageSeverities = messageSeverities;
		this.ruleName = ruleDefinition.getName();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Configure rule '" + ruleName + "'");
	}

	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);

		if (propertyValues.size() > 0) {
			GridLayout layout = new GridLayout();
			layout.numColumns = 1;

			Group group = new Group(parentComposite, SWT.NULL);
			group.setText("Properties");
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Composite composite = new Composite(group, SWT.NULL);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			composite.setLayout(layout);

			GridData labelData = new GridData();
			labelData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			// labelData.widthHint = 120;
			GridData textData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			textData.widthHint = 450;
			for (Map.Entry<String, String> entry : propertyValues.entrySet()) {
				Label label = new Label(composite, SWT.NONE);
				label.setText(validationRule.getPropertyDescription(entry.getKey()) + ":");
				label.setLayoutData(labelData);

				Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
				text.setText(entry.getValue());
				text.setLayoutData(textData);

				PropertyModifyListener listener = new PropertyModifyListener(text, entry.getKey(), validationRule
						.getRule().getClass());
				text.addModifyListener(listener);
				propertyModifyListeners.add(listener);
			}
		}

		if (messageSeverities.size() > 0) {
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;

			Group group = new Group(parentComposite, SWT.NULL);
			group.setText("Severities");
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Composite severityComposite = new Composite(group, SWT.NULL);
			severityComposite.setLayout(layout);
			severityComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

			GridData labelData = new GridData();
			labelData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			labelData.widthHint = 120;
			GridData textData = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			textData.widthHint = 450;

			for (Map.Entry<String, Integer> entry : messageSeverities.entrySet()) {
				Label label = new Label(severityComposite, SWT.NONE);
				label.setText(validationRule.getMessageLabel(entry.getKey()) + ":");
				label.setLayoutData(labelData);

				Combo text = new Combo(severityComposite, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
				text.setItems(new String[] { "Error", "Warning", "Info", "Ignore" });
				int severity = entry.getValue();
				switch (severity) {
				case IValidationProblemMarker.SEVERITY_ERROR:
					text.select(0);
					break;
				case IValidationProblemMarker.SEVERITY_WARNING:
					text.select(1);
					break;
				case IValidationProblemMarker.SEVERITY_INFO:
					text.select(2);
					break;
				default:
					text.select(3);
					break;
				}
				text.setLayoutData(textData);

				SeverityModifyListener listener = new SeverityModifyListener(text, entry.getKey());
				text.addModifyListener(listener);
				severityModifyListeners.add(listener);
			}
		}
		errorLabel = new Label(parentComposite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(JFaceColors.getErrorBackground(parent.getDisplay()));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			for (PropertyModifyListener listener : propertyModifyListeners) {
				propertyValues.put(listener.getName(), listener.getValue());
			}
			for (SeverityModifyListener listener : severityModifyListeners) {
				messageSeverities.put(listener.getName(), listener.getValue());
			}
		}
		super.buttonPressed(buttonId);
	}

	private void setErrorLabel(String error) {
		errorLabel.setText(error);
		okButton.setEnabled(!StringUtils.hasText(error));
	}

	private class PropertyModifyListener implements ModifyListener {

		private final Text text;

		private final String name;

		private final BeanWrapper wrapper;

		public PropertyModifyListener(Text text, String name, Class ruleClass) {
			this.wrapper = new BeanWrapperImpl(ruleClass);
			this.text = text;
			this.name = name;
		}

		public void modifyText(ModifyEvent e) {
			String value = getValue();
			try {
				wrapper.setPropertyValue(name, value);
				setErrorLabel("");
			}
			catch (BeansException ex) {
				setErrorLabel("Value '" + value + "' is not valid for property '" + name + "'");
			}
		}

		public String getValue() {
			return text.getText();
		}

		public String getName() {
			return name;
		}
	}

	private class SeverityModifyListener implements ModifyListener {
		private final Combo text;

		private final String name;

		public SeverityModifyListener(Combo text, String name) {
			this.text = text;
			this.name = name;
		}

		public Integer getValue() {
			String severity = text.getText();
			if ("Error".equals(severity)) {
				return IValidationProblemMarker.SEVERITY_ERROR;
			}
			else if ("Warning".equals(severity)) {
				return IValidationProblemMarker.SEVERITY_WARNING;
			}
			else if ("Info".equals(severity)) {
				return IValidationProblemMarker.SEVERITY_INFO;
			}
			else {
				return IValidationProblemMarker.SEVERITY_UNKOWN;
			}
		}

		public String getName() {
			return name;
		}

		public void modifyText(ModifyEvent e) {
		}
	}

}
