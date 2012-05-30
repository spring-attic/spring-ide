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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigDetailsSectionPart;
import org.springframework.ide.eclipse.config.ui.editors.overview.OverviewFormPage;


/**
 * @author Leo Dos Santos
 */
public class SpringConfigDetailsPropertySection extends AbstractConfigPropertySection {

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		if (input != null) {
			if (!pageBook.hasPage(input) && getConfigEditor() != null) {
				AbstractConfigFormPage page = getConfigEditor().getFormPageForAdapterUri(getInput().getNamespaceURI());
				if (page == null) {
					page = getConfigEditor().getFormPage(OverviewFormPage.ID);
				}
				if (page != null) {
					AbstractConfigDetailsPart detailsPart = page.getMasterDetailsBlock().getPage(getInput());
					if (detailsPart instanceof AbstractNamespaceDetailsPart) {
						AbstractNamespaceDetailsPart namespaceDetails = (AbstractNamespaceDetailsPart) detailsPart;
						Composite composite = pageBook.createPage(input);
						composite.setLayout(new GridLayout());
						SpringConfigDetailsSectionPart sectionPart = namespaceDetails.createDetailsSectionPart(
								getConfigEditor(), input, composite, getWidgetFactory());
						sectionPart.createContent();
						composite.setData(sectionPart);
						partMap.put(input, sectionPart);
					}
				}
			}
			pageBook.showPage(input);
		}
	}

}
