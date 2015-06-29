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
package org.springframework.ide.eclipse.boot.dash.views.sections;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Taggable;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabelProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Support for editing tags with the text cell editor
 *
 * @author Alex Boyko
 *
 */
public class TagEditingSupport extends EditingSupport {

	private TextCellEditor editor;

	public TagEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection) {
		super(viewer);
		this.editor = new TextCellEditor(viewer.getTable());
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return element instanceof Taggable;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof Taggable) {
			return StringUtils.join(((Taggable)element).getTags(), BootDashLabelProvider.TAGS_SEPARATOR);
		} else {
			return null;
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof Taggable && value instanceof String) {
			String str = (String) value;
			Taggable taggable = (Taggable) element;
			if (str.isEmpty()) {
				taggable.setTags(null);
			} else {
				taggable.setTags(new LinkedHashSet<String>(Arrays.asList(str.split("\\s+"))));
			}
		}
	}
	
}
