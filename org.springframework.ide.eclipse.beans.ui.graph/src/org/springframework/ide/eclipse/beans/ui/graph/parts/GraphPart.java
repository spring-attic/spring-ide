/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.graph.parts;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.springframework.ide.eclipse.beans.ui.graph.model.Graph;

public class GraphPart extends AbstractGraphicalEditPart {

	public static final int MARGIN_SIZE = 10;

	@Override
	protected IFigure createFigure() {
		Figure panel = new ScalableFreeformLayeredPane();
		panel.setBackgroundColor(ColorConstants.listBackground);
		panel.setLayoutManager(new XYLayout());
		panel.setBorder(new MarginBorder(MARGIN_SIZE));
		return panel;
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected List getModelChildren() {
		return ((Graph) super.getModel()).getNodes();
	}
}
