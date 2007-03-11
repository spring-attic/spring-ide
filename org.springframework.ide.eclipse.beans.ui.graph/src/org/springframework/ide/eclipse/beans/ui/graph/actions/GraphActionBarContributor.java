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

package org.springframework.ide.eclipse.beans.ui.graph.actions;

import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.actions.ActionFactory;

public class GraphActionBarContributor extends ActionBarContributor {

	@Override
	protected void buildActions() {
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
	}

	@Override
	protected void declareGlobalActionKeys() {
		addGlobalActionKey(ActionFactory.PRINT.getId());
	}

	@Override
	public void contributeToToolBar(IToolBarManager manager) {
		String[] zoomStrings = new String[] { ZoomManager.FIT_ALL,
								ZoomManager.FIT_HEIGHT, ZoomManager.FIT_WIDTH };
		manager.add(new ZoomComboContributionItem(getPage(), zoomStrings));
	}
}
