/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;

/**
 * The interface that has to be implemented to define a new runtarget type.
 *
 * @author Kris De Volder
 */
public interface RunTargetTypeFactory {
	RunTargetType create(BootDashModelContext context);
}
