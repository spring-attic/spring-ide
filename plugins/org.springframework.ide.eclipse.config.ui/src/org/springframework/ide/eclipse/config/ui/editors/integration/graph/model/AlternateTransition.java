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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class AlternateTransition extends Transition {

	public AlternateTransition(Activity source, Activity target, IDOMNode input) {
		super(source, target, input, Graphics.LINE_DASH);
	}

}
