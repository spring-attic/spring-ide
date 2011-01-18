/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.List;

/**
 * @author Christian Dupuis
 */
public interface IGlobalTransitions extends IWebflowModelElement {
	
	List<IStateTransition> getGlobalTransitions();

	void addGlobalTransition(IStateTransition action);

	void addGlobalTransition(IStateTransition action, int i);

	void removeGlobalTransition(IStateTransition action);

	void removeAll();

	void createNew(IWebflowModelElement parent);
}
