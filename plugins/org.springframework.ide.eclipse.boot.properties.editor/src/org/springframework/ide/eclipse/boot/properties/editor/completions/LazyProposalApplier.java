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
package org.springframework.ide.eclipse.boot.properties.editor.completions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.editor.support.completions.ProposalApplier;

/**
 * When creating the edits for a propoal is too expensive to repeat a few hundred times,
 * use this class to  wrap its creation so that the edits are only computed as needed.
 *
 * @author Kris De Volder
 */
public abstract class LazyProposalApplier implements ProposalApplier {

	private ProposalApplier created = null;
	private Exception exception = null;

	/**
	 * Implementers must provide this method, it will be called only
	 * if the proposal is being used.
	 */
	protected abstract ProposalApplier create() throws Exception;

	@Override
	public Point getSelection(IDocument document) throws Exception {
		ensureCreated();
		return created.getSelection(document);
	}

	@Override
	public void apply(IDocument doc) throws Exception {
		ensureCreated();
		created.apply(doc);
	}

	private void ensureCreated() throws Exception {
		if (created==null && exception==null) {
			try {
				this.created = create();
			} catch (Exception e) {
				this.exception = e;
			}
		}
		if (exception!=null) {
			throw exception;
		}
	}
}
