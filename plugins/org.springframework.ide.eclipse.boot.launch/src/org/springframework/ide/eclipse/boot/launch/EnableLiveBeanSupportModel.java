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
package org.springframework.ide.eclipse.boot.launch;

import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_ENABLE_LIVE_BEAN_SUPPORT;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setEnableLiveBeanSupport;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setJMXPort;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.launch.livebean.LiveBeanSupport;
import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabModel;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import static org.springsource.ide.eclipse.commons.livexp.core.ValidationResult.error;

/**
 * Model for 'enable live bean support' widgetry on a launchconfiguration tab.
 *
 * @author Kris De Volder
 */
public class EnableLiveBeanSupportModel implements ILaunchConfigurationTabModel {

	private static final int MAX_PORT = 65536;

	public final String portFieldName = "JMX Port";

	public final LiveVariable<Boolean> enabled;
	public final LiveVariable<String> port;
	private final Validator validator;

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(false);

	@SuppressWarnings("unchecked")
	public EnableLiveBeanSupportModel() {
		this.enabled = new LiveVariable<Boolean>(DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		this.port = new LiveVariable<String>("");
		enabled.addListener(makeDirty());
		port.addListener(makeDirty());
		this.validator = new Validator() {
			{
				dependsOn(enabled);
				dependsOn(port);
			}

			@Override
			protected ValidationResult compute() {
				boolean isEnabled = enabled.getValue();
				if (isEnabled) {
					String portStr = port.getValue();
					if (!hasText(portStr)) {
						return error(portFieldName+" must be specified");
					}
					try {
						int portValue = Integer.parseInt(portStr.trim());
						if (portValue<=0) {
							return error(portFieldName + " should be a positive integer");
						} else if (portValue>MAX_PORT) {
							return error(portFieldName + " should be smaller than "+MAX_PORT);
						}
					} catch (NumberFormatException e) {
						return error(portFieldName+" can't be parsed as an Integer");
					}
				}
				return ValidationResult.OK;
			}

		};
	}

	private boolean hasText(String portStr) {
		return portStr!=null && !portStr.trim().equals("");
	}

	@SuppressWarnings("rawtypes")
	protected ValueListener makeDirty() {
		return new ValueListener() {
			public void gotValue(LiveExpression exp, Object value) {
				dirtyState.setValue(true);
			}
		};
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		enabled.setValue(BootLaunchConfigurationDelegate.getEnableLiveBeanSupport(conf));
		port.setValue(BootLaunchConfigurationDelegate.getJMXPort(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setEnableLiveBeanSupport(conf, enabled.getValue());
		setJMXPort(conf, StringUtil.trim(port.getValue()));
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setEnableLiveBeanSupport(conf, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		setJMXPort(conf, ""+LiveBeanSupport.randomPort());
	}

	@Override
	public LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}
}
