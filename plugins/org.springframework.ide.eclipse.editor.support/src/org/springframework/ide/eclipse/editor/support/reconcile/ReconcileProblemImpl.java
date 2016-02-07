/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

/**
 * An implementation of {@link ReconcileProblem} that is just a simple data object.
 *
 * @author Kris De Volder
 */
public class ReconcileProblemImpl implements ReconcileProblem {

	final private ProblemType type;
	final private String msg;
	final private int offset;
	final private int len;

	public ReconcileProblemImpl(ProblemType type, String msg, int offset, int len) {
		super();
		this.type = type;
		this.msg = msg;
		this.offset = offset;
		this.len = len;
	}

	@Override
	public ProblemType getType() {
		return type;
	}

	@Override
	public String getMessage() {
		return msg;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public int getLength() {
		return len;
	}

}
