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
package org.springframework.ide.eclipse.boot.properties.editor.completions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;

/**
 * Interface that represents the methods that one needs to implement in order
 * to define how  content assist proposal is applied to a IDocument
 *
 * @author Kris De Volder
 */
public interface ProposalApplier {

	Point getSelection(IDocument document) throws Exception;
	void apply(IDocument doc) throws Exception;

}
