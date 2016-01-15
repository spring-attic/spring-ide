/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.completions;

import java.util.Collection;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlNavigable;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SNode;

/**
 * @author Kris De Volder
 */
public interface YamlAssistContext extends YamlNavigable<YamlAssistContext> {

	/**
	 * Create context sensitive completions.
	 */
	Collection<ICompletionProposal> getCompletions(YamlDocument doc, int offset, SNode node);
}
