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
package org.springframework.ide.eclipse.config.ui.editors.integration.stream.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.stream.graph.model.StderrChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.stream.graph.model.StdinChannelAdapterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.stream.graph.model.StdoutChannelAdapterModelElement;


/**
 * @author Leo Dos Santos
 */
public class IntStreamEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof StderrChannelAdapterModelElement) {
			part = new StderrChannelAdapterGraphicalEditPart((StderrChannelAdapterModelElement) model);
		}
		else if (model instanceof StdinChannelAdapterModelElement) {
			part = new StdinChannelAdapterGraphicalEditPart((StdinChannelAdapterModelElement) model);
		}
		else if (model instanceof StdoutChannelAdapterModelElement) {
			part = new StdoutChannelAdapterGraphicalEditPart((StdoutChannelAdapterModelElement) model);
		}
		return part;
	}

}
