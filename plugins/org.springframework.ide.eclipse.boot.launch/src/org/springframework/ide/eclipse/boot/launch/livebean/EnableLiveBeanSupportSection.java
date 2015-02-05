/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.livebean;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.launch.util.DelegatingLaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.UIConstants;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

/**
 * LaunchTabSection that enables LiveBean graph support by adding required
 * VM and program arguments.
 *
 * @author Kris De Volder
 */
public class EnableLiveBeanSupportSection extends DelegatingLaunchConfigurationTabSection {

	static class UI extends WizardPageSection {
		private Button checkbox;
		private Text portWidget;
		private EnableLiveBeanSupportModel model;

		public UI(IPageWithSections owner, EnableLiveBeanSupportModel model) {
			super(owner);
			this.model = model;
		}

		@Override
		public void createContents(Composite page) {
			Composite row = new Composite(page, SWT.NONE);
			row.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
			checkbox = new Button(row, SWT.CHECK);
			checkbox.setText("Enable Live Bean support.");
			checkbox.setToolTipText(computeTooltipText());
			Label label = new Label(row, SWT.NONE);
			label.setText("JMX Port:");
			portWidget = new Text(row, SWT.BORDER);
			GridDataFactory.fillDefaults().hint(UIConstants.fieldLabelWidthHint(portWidget, 6), SWT.DEFAULT)
				.applyTo(portWidget);

			model.enabled.addListener(new ValueListener<Boolean>() {
				public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
					boolean enable = value!=null && value;
					checkbox.setSelection(enable);
					portWidget.setEnabled(enable);
				}
			});
			checkbox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					model.enabled.setValue(checkbox.getSelection());
				}
			});

			model.port.addListener(new ValueListener<String>() {
				public void gotValue(LiveExpression<String> exp, String value) {
					String oldValue = portWidget.getText();
					if (!oldValue.equals(value)) {
						portWidget.setText(value);
					}
				}
			});
			portWidget.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					model.port.setValue(portWidget.getText());
				}
			});
		}

		private String computeTooltipText() {
			return "Enables support for Live Beans Graph View by adding vm args:\n" +
					LiveBeanSupport.liveBeanVmArgs("${jmxPort}");
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			return model.getValidator();
		}
	}

	public EnableLiveBeanSupportSection(IPageWithSections owner, EnableLiveBeanSupportModel model) {
		super(owner, model, new UI(owner, model));
	}

}
