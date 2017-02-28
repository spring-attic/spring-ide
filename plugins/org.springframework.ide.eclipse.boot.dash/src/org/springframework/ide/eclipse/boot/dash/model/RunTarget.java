/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.EnumSet;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreApi;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.util.template.Templates;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * A RunTarget represents an 'platform/environment' where we can 'Run' BootApps.
 *
 * TODO: launch configs are not applicable to all runtargets and methods relating
 * to launch configs do not belong in here. Remove and refactor!
 *
 * @author Kris De Volder
 */
public interface RunTarget extends IdAble, Nameable {

	public abstract RunTargetType getType();

	/**
	 * @return Subset of the runstate that a user can request when changing a
	 *         DashBoardElement's 'run-state'. Essentially, this allows
	 *         determining whether a given BootDahsElement can support the
	 *         'stop', 'run' and 'debug' operations which request that the
	 *         element be brought into a given run-state.
	 */
	public abstract EnumSet<RunState> supportedGoalStates();

	/**
	 * Create a launch config for a given dash element and initialize it with
	 * some suitable defaults.
	 *
	 * @param mainType,
	 *            may be null if the main type can not be 'guessed'
	 *            unambiguosly.
	 */
	public abstract ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception;

	public abstract BootDashColumn[] getAllColumns();

	public abstract BootDashColumn[] getDefaultColumns();

	/**
	 * Factory method to create the model for the 'elements tabel' of this run
	 * target.
	 */
	public abstract BootDashModel createElementsTabelModel(BootDashModelContext context, BootDashViewModel parent);

	/**
	 *
	 * @return true if it is a run target that can be deleted (and any
	 *         associated models). False otherwise
	 */
	public abstract boolean canRemove();

	/**
	 *
	 * @return true if this run target accepts application deployments. False otherwise.
	 */
	public abstract boolean canDeployAppsTo();

	/**
	 *
	 * @return true if applications can be deployed from this run target to other run targets. False otherwise.
	 */
	public abstract boolean canDeployAppsFrom();

	/**
	 * Provides a means to store persistent properties associated with this {@link RunTargetType}
	 */
	public abstract IPropertyStore getPropertyStore();

	/**
	 * A convenience method that provides access to the persisent property store returned by getPropertyStore
	 * through more convenient API.
	 */
	public abstract PropertyStoreApi getPersistentProperties();

	/**
	 * Return a nice name, suitable for displaying in a view to identify this target to the user.
	 */
	public abstract String getDisplayName();

	/**
	 * Customizable template used to create the diplayName. See {@link Templates} for the syntax.
	 * This may return null.
	 */
	public abstract String getNameTemplate();

	/**
	 * @return true if the String returned from getNameTemplate was specifically set on this target (instead of
	 * inherited from its type.
	 */
	public abstract boolean hasCustomNameTemplate();

	/**
	 * Set a custom name template for this target. Note that this only works on targets who provides support for
	 * persistent properties (since that's where this value is ultimately stored).
	 * <p>
	 * Setting the template to null makes the effective template be inherited from the runtarget type.
	 */
	public abstract void setNameTemplate(String template) throws Exception;

}
