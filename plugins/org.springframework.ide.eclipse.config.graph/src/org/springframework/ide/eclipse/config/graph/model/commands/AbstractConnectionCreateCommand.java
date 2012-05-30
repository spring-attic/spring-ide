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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.graph.model.Activity;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class AbstractConnectionCreateCommand extends AbstractTextCommand {

	protected Activity source;

	protected Activity target;

	protected IDOMElement sourceElement;

	protected IDOMElement targetElement;

	protected int lineStyle;

	public AbstractConnectionCreateCommand(ITextEditor textEditor, int lineStyle) {
		super(textEditor);
		this.lineStyle = lineStyle;
	}

	@Override
	public boolean canExecute() {
		if (source == null || target == null || source.equals(target)) {
			return false;
		}
		sourceElement = source.getInput();
		targetElement = target.getInput();
		if (sourceElement == null || targetElement == null) {
			return false;
		}
		return true;
	}

	public Activity getSource() {
		return source;
	}

	public Activity getTarget() {
		return target;
	}

	public void setSource(Activity activity) {
		source = activity;
	}

	public void setTarget(Activity activity) {
		target = activity;
	}

}
