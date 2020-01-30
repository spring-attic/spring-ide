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
 * TODO: This type shouldn't be necessary. RunTargetType is already a factory.
 * So this is a factry for a factory. It should be possible to just use RunTargetType
 * directly as a bean. To figure out how... investiaget {@link CloudFoundryRunTargetType}
 * and try to get rid of its reference to BootDashModelContext.
 *
 * @author Kris De Volder
 */
public interface RunTargetTypeFactory {
	RunTargetType create(BootDashModelContext context);
}
