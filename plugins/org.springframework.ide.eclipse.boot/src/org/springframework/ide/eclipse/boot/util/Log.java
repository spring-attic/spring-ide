/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.util;

import org.eclipse.core.runtime.IStatus;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class Log {

	public static void log(Throwable e) {
		if (ExceptionUtil.isCancelation(e)) {
			//Don't log canceled operations, those aren't real errors.
			return;
		}
		try {
			BootActivator.getDefault().getLog().log(ExceptionUtil.status(e));
		} catch (NullPointerException npe) {
			//Can happen if errors are trying to be logged during Eclipse's shutdown
			e.printStackTrace();
		}
	}

	public static void info(String msg) {
		BootActivator.getDefault().getLog().log(ExceptionUtil.status(IStatus.INFO, msg));
	}

	public static void warn(String msg) {
		BootActivator.getDefault().getLog().log(ExceptionUtil.status(IStatus.WARNING, msg));
	}

	public static void warn(Throwable e) {
		if (ExceptionUtil.isCancelation(e)) {
			//Don't log canceled operations, those aren't real errors.
			return;
		}
		try {
			BootActivator.getDefault().getLog().log(ExceptionUtil.status(IStatus.WARNING, e));
		} catch (NullPointerException npe) {
			//Can happen if errors are trying to be logged during Eclipse's shutdown
			e.printStackTrace();
		}
	}

}
