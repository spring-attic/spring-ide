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
package org.springframework.ide.eclipse.boot.dash.model;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

public abstract class Operation {

	private final String opName;

	public Operation(String opName) {
		this.opName = opName;
	}

	public void run(IProgressMonitor monitor) throws Exception {
		SubMonitor sub = SubMonitor.convert(monitor);
		sub.subTask(opName);
		runOp(sub.newChild(100));
	}

	protected abstract void runOp(IProgressMonitor monitor) throws Exception;

	public static void runForked(final Operation op, IRunnableContext context) throws Exception {

		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					op.run(monitor);
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			context.run(true, true, runnable);
		} catch (InvocationTargetException ite) {
			// Don't throw the wrapper
			throw ite.getTargetException() instanceof Exception ? (Exception) ite.getTargetException() : ite;
		}
	}

	public static void runForked(Operation runnable) throws Exception {
//		runForked(runnable, PlatformUI.getWorkbench().getService(IProgressService.class));
	}
}