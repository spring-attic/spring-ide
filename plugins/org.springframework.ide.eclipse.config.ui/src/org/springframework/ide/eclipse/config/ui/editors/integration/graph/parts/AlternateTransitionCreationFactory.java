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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.gef.requests.CreationFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AlternateTransition;


/**
 * @author Leo Dos Santos
 */
public class AlternateTransitionCreationFactory implements CreationFactory {

	public Object getNewObject() {
		return null;
	}

	public Object getObjectType() {
		return AlternateTransition.class;
	}

}
