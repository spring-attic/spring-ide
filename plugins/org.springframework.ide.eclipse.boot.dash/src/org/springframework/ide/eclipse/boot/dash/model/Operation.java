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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

public abstract class Operation<T> {

	private final String opName;

	public Operation(String opName) {
		this.opName = opName;
	}

	public String getName() {
		return this.opName;
	}

	public T run(IProgressMonitor monitor) throws Exception {
		SubMonitor sub = SubMonitor.convert(monitor);
		sub.subTask(opName);
		return runOp(sub.newChild(100));
	}

	protected abstract T runOp(IProgressMonitor monitor) throws Exception;

	public T run(IRunnableContext context, boolean fork) throws Exception {

		final List<T> val = new ArrayList<T>();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					val.add(Operation.this.run(monitor));
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			context.run(fork, true, runnable);

		} catch (InvocationTargetException ite) {
			// Don't throw the wrapper
			throw ite.getTargetException() instanceof Exception ? (Exception) ite.getTargetException() : ite;
		}
		return val.get(0);
	}
}