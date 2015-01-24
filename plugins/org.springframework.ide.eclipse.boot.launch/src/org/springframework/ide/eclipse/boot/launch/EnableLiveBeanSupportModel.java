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

import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

import static org.springsource.ide.eclipse.commons.livexp.core.ValidationResult.error;
import static org.springframework.ide.eclipse.boot.util.StringUtil.*;

/**
 * Model for 'enable live bean support' widgetry on a launchconfiguration tab.
 *
 * @author Kris De Volder
 */
public class EnableLiveBeanSupportModel {

	private static final int MAX_PORT = 65536;

	public final String portFieldName = "JMX Port";

	public final LiveVariable<Boolean> enabled;
	public final LiveVariable<String> port;
	public final Validator validator;

	public EnableLiveBeanSupportModel() {
		this.enabled = new LiveVariable<Boolean>(DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		this.port = new LiveVariable<String>("");
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
						int portValue = Integer.parseInt(portStr);
						if (portValue<=0) {
							return error(portFieldName + " should be a positive integer");
						} else if (portValue>MAX_PORT) {
							return error(portFieldName + " should smaller than "+MAX_PORT);
						}
					} catch (NumberFormatException e) {
						return error(portFieldName+" can't be parsed as an Integer");
					}
				}
				return ValidationResult.OK;
			}
		};
	}

}
