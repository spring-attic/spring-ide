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

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class MoveCommand extends Command {

	private final Activity activity;

	private final Rectangle oldBounds;

	private final Rectangle newBounds;

	public MoveCommand(Activity activity, Rectangle oldBounds, Rectangle newBounds) {
		this.activity = activity;
		this.oldBounds = oldBounds;
		this.newBounds = newBounds;
	}

	@Override
	public void execute() {
		activity.modifyBounds(newBounds);
	}

	@Override
	public void undo() {
		activity.modifyBounds(oldBounds);
	}

}
