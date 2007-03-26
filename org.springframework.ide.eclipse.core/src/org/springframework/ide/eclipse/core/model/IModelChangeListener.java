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
package org.springframework.ide.eclipse.core.model;

/**
 * An element changed listener receives notification of changes to elements
 * maintained by the model.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * @author Torsten Juergeleit
 */
public interface IModelChangeListener {

	/**
	 * Notifies that one or more attributes of one or more model elements have
	 * changed. The specific details of the change are described by the given
	 * event.
	 * @param event  the change event
	 */
	public void elementChanged(ModelChangeEvent event);
}
