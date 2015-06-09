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

import java.util.EnumSet;

import org.eclipse.core.runtime.Platform;
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
import org.springframework.ide.eclipse.boot.launch.livebean.JmxBeanSupport.Feature;
import org.springframework.ide.eclipse.boot.launch.util.DelegatingLaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
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
public class EnableJmxSection extends DelegatingLaunchConfigurationTabSection {

	private static final boolean DEBUG = false;//(""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
 	}


	static class UI extends WizardPageSection {
		private Button liveBeanCheckbox;
		private Button lifeCycleCheckbox;
		private Text portWidget;
		private EnableJmxFeaturesModel model;

		public UI(IPageWithSections owner, EnableJmxFeaturesModel model) {
			super(owner);
			this.model = model;
		}

		@Override
		public void createContents(Composite page) {
			Composite row = new Composite(page, SWT.NONE);
			row.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
			liveBeanCheckbox = new Button(row, SWT.CHECK);
			liveBeanCheckbox.setText("Enable Live Bean support.");
			liveBeanCheckbox.setToolTipText(computeTooltipText(
					"Enables support for Live Beans Graph View by adding vm args:\n",
					Feature.LIVE_BEAN_GRAPH));

			Label label = new Label(row, SWT.NONE);
			label.setText("JMX Port:");
			portWidget = new Text(row, SWT.BORDER);
			GridDataFactory.fillDefaults().hint(UIConstants.fieldLabelWidthHint(portWidget, 6), SWT.DEFAULT)
				.applyTo(portWidget);

			lifeCycleCheckbox = new Button(row, SWT.CHECK);
			lifeCycleCheckbox.setText("Enable Life Cycle Tracking.");
			lifeCycleCheckbox.setToolTipText(computeTooltipText(
					"Allows Boot Dashboard View to track 'STARTING' state of Boot Apps (require Boot 1.3.0). " +
					"Adds these vm args: \n",
					Feature.LIFE_CYCLE));

			model.anyFeatureEnabled.addListener(new ValueListener<Boolean>() {
				public void gotValue(LiveExpression<Boolean> exp, Boolean enable) {
					debug("anyFeature : "+enable);
					portWidget.setEnabled(enable);
				}
			});

			connectCheckbox(model.liveBeanEnabled, liveBeanCheckbox);
			connectCheckbox(model.lifeCycleEnabled, lifeCycleCheckbox);

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


		private String computeTooltipText(String baseMsg, Feature feature) {
			return baseMsg +
					JmxBeanSupport.jmxBeanVmArgs("${jmxPort}", EnumSet.of(feature));
		}

		@Override
		public LiveExpression<ValidationResult> getValidator() {
			return model.getValidator();
		}
	}

	public EnableJmxSection(IPageWithSections owner, EnableJmxFeaturesModel model) {
		super(owner, model, new UI(owner, model));
	}

	private static void connectCheckbox(final LiveVariable<Boolean> checkedState, final Button widget) {
		final String name = widget.getText();
		checkedState.addListener(new ValueListener<Boolean>() {
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				boolean enable = value!=null && value;
				debug("Widget '"+name+"' <- "+enable);
				widget.setSelection(enable);
			}

		});
		widget.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean enable = widget.getSelection();
				debug("Model '"+name+"' <- "+enable);
				checkedState.setValue(enable);
			}
		});
	}

}
