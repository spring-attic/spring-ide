/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.FieldContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for handling content
 * assist request on elements of the <code>util:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class UtilContentAssistProcessor extends
		NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		registerContentAssistCalculator("list-class",
				new ClassHierachyContentAssistCalculator(List.class.getName()));
		registerContentAssistCalculator("map-class",
				new ClassHierachyContentAssistCalculator(Map.class.getName()));
		registerContentAssistCalculator("set-class",
				new ClassHierachyContentAssistCalculator(Set.class.getName()));
		ClassContentAssistCalculator calculator = new ClassContentAssistCalculator();
		registerContentAssistCalculator("value-type", calculator);
		registerContentAssistCalculator("key-type", calculator);

		registerContentAssistCalculator("constant", "static-field",
				new FieldContentAssistCalculator());

	}
}
