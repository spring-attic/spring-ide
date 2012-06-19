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
package org.springframework.ide.eclipse.roo.core.commands;

import java.util.List;

import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameter;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandParameterDescriptor;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.IFrameworkCommand;


/**
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class RooCommandUtils {

	public static String constructCommandString(IFrameworkCommand command) {

		if (command == null) {
			return null;
		}

		final StringBuffer actualCommand = new StringBuffer();
		actualCommand.append(command.getCommandDescriptor().getName());
		actualCommand.append(" ");
		List<ICommandParameter> values = command.getParameters();
		if (values != null) {
			for (ICommandParameter value : values) {
				if (value.hasValue()) {

					ICommandParameterDescriptor descriptor = value.getParameterDescriptor();

					if (descriptor.requiresParameterNameInCommand()) {
						String prefix = descriptor.getParameterPrefix();
						String valueSeparator = descriptor.getValueSeparator();
						String name = descriptor.getName();
						if (prefix != null) {
							actualCommand.append(prefix);
						}
						actualCommand.append(name);
						if (valueSeparator != null) {
							actualCommand.append(valueSeparator);
						}
					}

					actualCommand.append(value.getValue());
					actualCommand.append(" ");
				}
			}
		}
		return actualCommand.toString();
	}

}
