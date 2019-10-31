/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jface.action.IAction;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public interface DynamicSubMenuSupplier extends Supplier<List<IAction>> {
	default boolean isVisible() { return true; }
	default LiveExpression<Boolean> isEnabled() { return LiveExpression.constant(true); }
}
