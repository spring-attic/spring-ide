/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowStateValidationRule implements
		IValidationRule<WebflowState, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof WebflowState && context instanceof WebflowValidationContext;
	}

	public void validate(WebflowState state, WebflowValidationContext context,
			IProgressMonitor monitor) {

		if (context.isVersion1()) {
			if (state.getStartState() == null) {
				Element node = (Element) state.getNode();
				if (node != null) {
					NodeList startStateNodes = node.getElementsByTagName("start-state");
					if (startStateNodes == null || startStateNodes.getLength() == 0) {
						context.error(state, "NO_START_STATE",
								"Start state definition is missing. Add a 'start-state' element");
					}
					else if (startStateNodes.getLength() == 1) {
						IDOMNode startStateNode = (IDOMNode) startStateNodes.item(0);
						String idref = state.getAttribute(startStateNode, "idref");
						if (idref == null) {
							context.error(state, "NO_START_STATE_IDREF",
									"Start state definition misses 'idref' attribute");
						}
						else if (idref != null) {
							context.error(state, "NO_START_STATE_IDREF_INVALID", MessageUtils
								.format("Start state definition references non-existing state \"{0}\"",
										idref));
						}
					}
				}
				else {
					context.error(state, "NO_FLOW", "Flow definition misses 'flow' element");

				}
			}
		}
		else {
			
			// An abstract flow does not need to have states
			if (!"true".equalsIgnoreCase(state.getAbstract())) {
				if (state.getStartState() == null) {
					context.error(state, "NO_START_STATE",
							"Start state is missing. Add at least one state to the flow");
				}
				else {
					Element node = (Element) state.getNode();
					if (node.getAttribute("start-state") != null) {
						String idref = state.getAttribute((IDOMNode) node, "start-state");
						if (idref != null
								&& WebflowModelXmlUtils.getStateById(state, idref) == null) {
							context.error(state, "NO_START_STATE", "Start state definition is not " +
									"pointing to an exiting state. Correct the value of the 'start-state' attribute");
						}
					}
				}
			}
		}
	}
	
}
