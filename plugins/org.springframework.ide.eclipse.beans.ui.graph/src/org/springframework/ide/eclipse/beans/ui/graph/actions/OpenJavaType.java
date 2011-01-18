/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.parts.BeanPart;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * This action opens the selected bean's Java type.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class OpenJavaType extends EditorPartAction {

	public OpenJavaType(IEditorPart editor) {
		super(editor);
	}

	@Override
	protected void init() {
		setId(GraphActionConstants.OPEN_TYPE);
		setText(BeansGraphPlugin.getResourceString(
				"ContextMenu.OpenType.text"));
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		EditPart part = getFirstSelectedEditPart();
		if (part instanceof BeanPart) {
			Bean bean = ((BeanPart) part).getBean();
			if (bean.isRootBean() && bean.getClassName() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		IBean bean = ((BeanPart) getFirstSelectedEditPart()).getBean()
				.getBean();
		IType type = BeansModelUtils.getBeanType(bean, null);
		if (type != null) {
			SpringUIUtils.openInEditor(type);
		}
	}

	protected EditPart getFirstSelectedEditPart() {
		GraphicalViewer viewer = ((GraphEditor) getWorkbenchPart())
				.getGraphicalViewer();
		List list = viewer.getSelectedEditParts();
		if (!list.isEmpty()) {
			return (EditPart) list.get(0);
		}
		return null;
	}
}
