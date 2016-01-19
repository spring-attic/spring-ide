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

import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_ENABLE_LIFE_CYCLE;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_ENABLE_LIVE_BEAN_SUPPORT;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_TERMINATION_TIMEOUT;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setEnableLifeCycle;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setEnableLiveBeanSupport;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setJMXPort;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setTerminationTimeout;
import static org.springsource.ide.eclipse.commons.livexp.core.ValidationResult.error;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.ILaunchConfigurationTabModel;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.OrExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Model for 'enable live bean support' widgetry on a launchconfiguration tab.
 *
 * @author Kris De Volder
 */
public class EnableJmxFeaturesModel implements ILaunchConfigurationTabModel {

	private static final int MAX_PORT = 65536;


	public final String portFieldName = "JMX Port";
	public final String timeOutFieldName = "Termination timeout";

	public final LiveVariable<Boolean> liveBeanEnabled;
	public final LiveVariable<Boolean> lifeCycleEnabled;
	public final LiveExpression<Boolean> anyFeatureEnabled;

	public final LiveVariable<String> port;
	public final LiveVariable<String> terminationTimeout;

	private final Validator validator;

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(false);


	@SuppressWarnings("unchecked")
	public EnableJmxFeaturesModel() {
		this.liveBeanEnabled = new LiveVariable<Boolean>(DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		this.lifeCycleEnabled = new LiveVariable<Boolean>(DEFAULT_ENABLE_LIFE_CYCLE);
		this.anyFeatureEnabled = new OrExpression(liveBeanEnabled, lifeCycleEnabled);

		this.port = new LiveVariable<String>("");
		this.terminationTimeout = new LiveVariable<String>("");
		liveBeanEnabled.addListener(makeDirty());
		lifeCycleEnabled.addListener(makeDirty());
		port.addListener(makeDirty());
		terminationTimeout.addListener(makeDirty());

		this.validator = new Validator() {
			{
				dependsOn(anyFeatureEnabled);
				dependsOn(port);
				dependsOn(terminationTimeout);
			}

			@Override
			protected ValidationResult compute() {
				boolean isEnabled = anyFeatureEnabled.getValue();
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
				if (lifeCycleEnabled.getValue()) {
					String timeoutStr = terminationTimeout.getValue();
					if (!hasText(timeoutStr)) {
						return error(timeOutFieldName+" must be specified");
					}
					timeoutStr = timeoutStr.trim();
					try {
						long timeout = Long.parseLong(timeoutStr);
						if (!(timeout > 0)) {
							return error(timeOutFieldName+" must be positive");
						}
					} catch (NumberFormatException e) {
						return error(timeOutFieldName+" can't be parsed as an Integer");
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
		liveBeanEnabled.setValue(BootLaunchConfigurationDelegate.getEnableLiveBeanSupport(conf));
		lifeCycleEnabled.setValue(BootLaunchConfigurationDelegate.getEnableLifeCycle(conf));
		port.setValue(BootLaunchConfigurationDelegate.getJMXPort(conf));
		terminationTimeout.setValue(""+BootLaunchConfigurationDelegate.getTerminationTimeout(conf));
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setEnableLiveBeanSupport(conf, liveBeanEnabled.getValue());
		setEnableLifeCycle(conf, lifeCycleEnabled.getValue());
		setJMXPort(conf, StringUtil.trim(port.getValue()));
		setTerminationTimeout(conf, StringUtil.trim(terminationTimeout.getValue()));
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setEnableLiveBeanSupport(conf, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		setEnableLifeCycle(conf, DEFAULT_ENABLE_LIFE_CYCLE);
		setJMXPort(conf, ""+JmxBeanSupport.randomPort());
		setTerminationTimeout(conf, ""+DEFAULT_TERMINATION_TIMEOUT);
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
