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
package org.springframework.ide.eclipse.config.ui.editors.integration.mail.graph.model;

import java.util.Arrays;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntMailSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class MailToStringTransformerModelElement extends Activity {

	public MailToStringTransformerModelElement() {
		super();
	}

	public MailToStringTransformerModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntMailSchemaConstants.ELEM_MAIL_TO_STRING_TRANSFORMER;
	}

	@Override
	public List<String> getPrimaryIncomingAttributes() {
		return Arrays.asList(IntMailSchemaConstants.ATTR_INPUT_CHANNEL);
	}

	@Override
	public List<String> getPrimaryOutgoingAttributes() {
		return Arrays.asList(IntMailSchemaConstants.ATTR_OUTPUT_CHANNEL);
	}

}
