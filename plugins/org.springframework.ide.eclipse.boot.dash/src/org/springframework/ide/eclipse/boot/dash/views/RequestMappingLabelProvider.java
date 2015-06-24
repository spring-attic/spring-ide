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
package org.springframework.ide.eclipse.boot.dash.views;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;

/**
 * @author Kris De Volder
 */
public class RequestMappingLabelProvider extends CellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object el = cell.getElement();
		if (el instanceof RequestMapping) {
			cell.setText(((RequestMapping) el).getPath());
		} else {
			cell.setText(""+el);
		}
	}


}
