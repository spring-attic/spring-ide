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
package org.springframework.ide.eclipse.wizard.template.infrastructure.ui;

import java.util.Map;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public interface WizardUI {
	public void collectInput(Map<String, Object> collectedInput, Map<String, String> inputKinds);
}
