/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * {@link IModelElementVisitor} that visits every element regardless of type or
 * any other attribute.
 * <p>
 * Note: this visitor will visit the complete model graph down to every leaf.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class TrueModelElementVisitor implements IModelElementVisitor {

	public boolean visit(IModelElement element, IProgressMonitor monitor) {
		return true;
	}
}