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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.model.Taggable;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Support for editing tags with the text cell editor
 *
 * @author Alex Boyko
 *
 */
public class TagEditingSupport extends EditingSupport {

	private StyledTextCellEditor editor;
	private Stylers stylers;

	public TagEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection, Stylers stylers) {
		super(viewer);
		this.stylers = stylers;
		this.editor = new TagsCellEditor(viewer.getTable());
	}

	public TagEditingSupport(TableViewer viewer, LiveExpression<BootDashElement> selection, BootDashViewModel model, Stylers stylers) {
		super(viewer);
		this.stylers = stylers;
		IContentProposalProvider proposalProvider = new TagContentProposalProvider(model);
		this.editor = new TagsCellEditor(viewer.getTable(), proposalProvider, UIUtils.CTRL_SPACE,
				UIUtils.TAG_CA_AUTO_ACTIVATION_CHARS).setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
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
			return TagUtils.toString(((Taggable)element).getTags());
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
				taggable.setTags(new LinkedHashSet<String>(Arrays.asList(TagUtils.parseTags(str))));
			}
		}
	}

	private class TagsCellEditor extends StyledTextCellEditor {

		TagsCellEditor(Composite parent) {
			super(parent);
		}

		TagsCellEditor(Composite parent, IContentProposalProvider contentProposalProvider,
				KeyStroke keyStroke, char[] autoActivationCharacters) {
			super(parent, contentProposalProvider, keyStroke, autoActivationCharacters);
		}

		@Override
		protected StyleRange[] updateStyleRanges(String text) {
			StyledString styled = UIUtils.getStyleRangesForTags(text, stylers.tag());
			return styled.getStyleRanges();
		}

	}

}
