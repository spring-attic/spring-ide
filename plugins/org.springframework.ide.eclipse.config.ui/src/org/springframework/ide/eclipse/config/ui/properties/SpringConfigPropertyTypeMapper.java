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
package org.springframework.ide.eclipse.config.ui.properties;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.views.properties.tabbed.AbstractTypeMapper;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.Transition;
import org.springframework.ide.eclipse.config.graph.parts.ActivityPart;
import org.springframework.ide.eclipse.config.graph.parts.TransitionPart;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class SpringConfigPropertyTypeMapper extends AbstractTypeMapper {

	@SuppressWarnings("rawtypes")
	@Override
	public Class mapType(Object object) {
		if (object instanceof IDOMNode) {
			return object.getClass();
		}
		if (object instanceof TreeItem) {
			TreeItem item = (TreeItem) object;
			return mapType(item.getData());
		}
		if (object instanceof ActivityPart) {
			ActivityPart part = (ActivityPart) object;
			Activity model = part.getModelElement();
			return mapType(model.getInput());
		}
		if (object instanceof TransitionPart) {
			TransitionPart part = (TransitionPart) object;
			Transition trans = (Transition) part.getModel();
			return mapType(trans.getInput());
		}
		return super.mapType(object);
	}

}
