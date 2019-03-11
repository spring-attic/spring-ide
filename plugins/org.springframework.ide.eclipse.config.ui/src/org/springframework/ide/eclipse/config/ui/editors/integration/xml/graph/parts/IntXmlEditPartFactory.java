/*******************************************************************************
 *  Copyright (c) 2012, 2014 Pivotal Software Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.RouterGraphicalPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.MarshallingTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.UnmarshallingTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.ValidatingFilterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.XpathFilterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.XpathHeaderEnricherModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.XpathRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.XpathSplitterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.XpathTransformerModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xml.graph.model.XslTransformerModelElement;

/**
 * @author Leo Dos Santos
 */
public class IntXmlEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof MarshallingTransformerModelElement) {
			part = new MarshallingTransformerGraphicalEditPart((MarshallingTransformerModelElement) model);
		}
		else if (model instanceof UnmarshallingTransformerModelElement) {
			part = new UnmarshallingTransformerGraphicalEditPart((UnmarshallingTransformerModelElement) model);
		}
		else if (model instanceof ValidatingFilterModelElement) {
			part = new ValidatingFilterGraphicalEditPart((ValidatingFilterModelElement) model);
		}
		else if (model instanceof XpathFilterModelElement) {
			part = new XpathFilterGraphicalEditPart((XpathFilterModelElement) model);
		}
		else if (model instanceof XpathHeaderEnricherModelElement) {
			part = new XpathHeaderEnricherGraphicalEditPart((XpathHeaderEnricherModelElement) model);
		}
		else if (model instanceof XpathRouterModelElement) {
			part = new RouterGraphicalPart((XpathRouterModelElement) model, IntegrationImages.ROUTER,
					IntegrationImages.BADGE_SI_XML);
		}
		else if (model instanceof XpathSplitterModelElement) {
			part = new XpathSplitterGraphicalEditPart((XpathSplitterModelElement) model);
		}
		else if (model instanceof XpathTransformerModelElement) {
			part = new XpathTransformerGraphicalEditPart((XpathTransformerModelElement) model);
		}
		else if (model instanceof XslTransformerModelElement) {
			part = new XsltTransformerGraphicalEditPart((XslTransformerModelElement) model);
		}
		return part;
	}
}
