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
package org.springframework.ide.eclipse.config.graph.model.commands;

import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.model.StructuredActivity;
import org.springframework.ide.eclipse.config.graph.model.Transition;


/**
 * SplitTransitionCommand
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SplitTransitionCommand extends AbstractTextCommand {

	private StructuredActivity parent;

	private Activity oldSource;

	private Activity oldTarget;

	private Transition transition;

	private Activity newActivity;

	public SplitTransitionCommand(ITextEditor textEditor) {
		super(textEditor);
	}

	@Override
	public boolean canExecute() {
		return false;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		oldSource.removeOutgoing(transition);
		oldTarget.removeIncoming(transition);
		parent.addChild(newActivity);
		new Transition(oldSource, newActivity);
		new Transition(newActivity, oldTarget);
	}

	/**
	 * Sets the Activity to be added.
	 * @param activity the new activity
	 */
	public void setNewActivity(Activity activity) {
		newActivity = activity;
	}

	/**
	 * Sets the parent Activity. The new Activity will be added as a child to
	 * the parent.
	 * @param activity the parent
	 */
	public void setParent(StructuredActivity activity) {
		parent = activity;
	}

	/**
	 * Sets the transition to be "split".
	 * @param transition the transition to be "split".
	 */
	public void setTransition(Transition transition) {
		this.transition = transition;
		oldSource = transition.source;
		oldTarget = transition.target;
	}

}
