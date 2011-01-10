/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.Graph;
import org.springframework.ide.eclipse.beans.ui.graph.model.Reference;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class GraphicalPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof Graph) {
			part = new GraphPart();
		} else if (model instanceof Bean) {
			part = new BeanPart();
		} else if (model instanceof Reference) {
			part = new ReferencePart();
		}
		part.setModel(model);
		return part;
	}
}
