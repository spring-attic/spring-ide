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

import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_ENABLE_LIVE_BEAN_SUPPORT;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setEnableLiveBeanSupport;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setJMXPort;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.EnableLiveBeanSupportModel;
import org.springframework.ide.eclipse.boot.launch.util.LaunchConfigurationTabSection;
import org.springframework.ide.eclipse.boot.launch.util.WidgetSync;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.UIConstants;

/**
 * LaunchTabSection that enables LiveBean graph support by adding required
 * VM and program arguments.
 *
 * @author Kris De Volder
 */
public class EnableLiveBeanSupportSection extends LaunchConfigurationTabSection {

	private Button enableLiveBeans;
	private Text port;

	private EnableLiveBeanSupportModel model;

	public EnableLiveBeanSupportSection(IPageWithSections owner) {
		this(owner, new EnableLiveBeanSupportModel());
	}

	public EnableLiveBeanSupportSection(IPageWithSections owner, EnableLiveBeanSupportModel model) {
		super(owner);
		this.model = model;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return model.validator;
	}

	@Override
	public void createContents(Composite page) {
		Composite row = new Composite(page, SWT.NONE);
		row.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		enableLiveBeans = new Button(row, SWT.CHECK);
		enableLiveBeans.setText("Enable Live Bean support.");
		enableLiveBeans.setToolTipText(computeTooltipText());
		Label label = new Label(row, SWT.NONE);
		label.setText("JMX Port:");
		port = new Text(row, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(UIConstants.fieldLabelWidthHint(port, 6), SWT.DEFAULT)
			.applyTo(port);

		WidgetSync.bindCheckbox(enableLiveBeans, model.enabled, getDirtyState());
		WidgetSync.bindText(port, model.port, getDirtyState());
		model.enabled.addListener(new ValueListener<Boolean>() {
			public void gotValue(LiveExpression<Boolean> exp, Boolean value) {
				boolean enable = enableLiveBeans.getSelection();
				port.setEnabled(enable);
			}
		});
	}

	private String computeTooltipText() {
		return "Enables support for Live Beans Graph View by adding vm args:\n" +
				LiveBeanSupport.liveBeanVmArgs("${jmxPort}");
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		model.enabled.setValue(BootLaunchConfigurationDelegate.getEnableLiveBeanSupport(conf));
		model.port.setValue(BootLaunchConfigurationDelegate.getJMXPort(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setEnableLiveBeanSupport(conf, model.enabled.getValue());
		setJMXPort(conf, model.port.getValue());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setEnableLiveBeanSupport(conf, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		setJMXPort(conf, ""+LiveBeanSupport.randomPort());
	}

}
