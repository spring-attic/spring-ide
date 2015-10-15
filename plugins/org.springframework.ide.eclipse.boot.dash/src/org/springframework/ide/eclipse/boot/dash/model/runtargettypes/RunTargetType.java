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
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSet;

/**
 * A run target type represents a type of 'deployment environment' to which
 * boot apps can be targetted to run. For example 'local', 'cloudfoundry'
 * or 'lattice'.
 *
 * @author Kris De Volder
 */
public interface RunTargetType extends Nameable {

	/**
	 * @return Whether it is possible to create instances of this type. Not all
	 * runtargets provide this ability. For example the 'local' run target
	 * is a singleton and doesn't allow creating instances.
	 */
	boolean canInstantiate();

	/**
	 * RunTargetTypes that return 'true' from 'canCreate' must provide an implementation
	 * of this method. When called it opens a UI allowing the user to create a new
	 * run target.
	 */
	void openTargetCreationUi(LiveSet<RunTarget> targets);

	/**
	 *
	 * @return if {@link #canInstantiate()} returns true, return a new {@link RunTarget}. Return null if this
	 * type cannot be instantiated.
	 */
	RunTarget createRunTarget(TargetProperties properties);

	ImageDescriptor getIcon();

}
