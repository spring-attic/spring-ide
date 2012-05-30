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
package org.springframework.ide.eclipse.config.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public abstract class LabelledTransition extends Transition {

	public LabelledTransition(Activity source, Activity target, IDOMElement input) {
		super(source, target, input);
	}

	@Override
	public IDOMElement getInput() {
		return (IDOMElement) super.getInput();
	}

	public abstract String getLabel();

}
