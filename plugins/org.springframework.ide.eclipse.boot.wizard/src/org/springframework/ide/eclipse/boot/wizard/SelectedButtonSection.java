/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.function.Supplier;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;
import org.springsource.ide.eclipse.commons.livexp.util.Filter;

public class SelectedButtonSection<T> extends WizardPageSection {

	private static final String X_LABEL = "X";
	private Composite buttonComp;
	private ValueListener<Boolean> selectionListener;
	private Stylers stylers;
	
	protected final CheckBoxModel<T> model;
	protected final LiveVariable<Boolean> isVisible = new LiveVariable<Boolean>(true);

	public SelectedButtonSection(IPageWithSections owner, CheckBoxModel<T> model) {
		super(owner);
		this.model = model;
	}

	@Override
	public void createContents(Composite page) {
		if (page != null && !page.isDisposed()) {
			buttonComp = new Composite(page, SWT.NONE);

			GridDataFactory.fillDefaults().grab(false, false).applyTo(buttonComp);

			createButtonArea();
			isVisible.addListener(new ValueListener<Boolean>() {
				public void gotValue(LiveExpression<Boolean> exp, Boolean reveal) {
					if (reveal != null && buttonComp != null && !buttonComp.isDisposed()) {
						buttonComp.setVisible(reveal);
						GridData data = (GridData) buttonComp.getLayoutData();
						data.exclude = !reveal;
					}
				}
			});
		}
	}

	private void createButtonArea() {
		GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 0).spacing(10, 0).applyTo(buttonComp);
		StyledText xButton = new StyledText(buttonComp, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(xButton);

		stylers = new Stylers(xButton.getFont());
		xButton.setCursor(new Cursor(xButton.getDisplay(), SWT.CURSOR_ARROW));
		xButton.setBackground(buttonComp.getBackground());
		xButton.setEditable(false);
		applyDefaultX(xButton);
		
		xButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent event) {
				// Do nothing
			}

			@Override
			public void mouseDown(MouseEvent event) {
				removeSelection();
			}

			@Override
			public void mouseDoubleClick(MouseEvent event) {
				removeSelection();
			}

		});

		xButton.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent event) {
				// Do nothing. Enter/exit events are faster than hover, so those will
				// be fired first before hover
			}

			@Override
			public void mouseExit(MouseEvent event) {
				applyDefaultX(xButton);
			}

			@Override
			public void mouseEnter(MouseEvent event) {
				applyBoldX(xButton);
			}
		});

		xButton.setToolTipText("Click to remove the dependency");

		Label label = new Label(buttonComp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).hint(170, SWT.DEFAULT)
				.applyTo(label);

		label.setText(model.getLabel());
		Supplier<String> tooltip = model.getTooltip();
		if (tooltip != null) {
			HtmlTooltip htmlTooltip = new HtmlTooltip(label);
			htmlTooltip.setShift(new Point(0, 0));
			htmlTooltip.setHtml(tooltip);
		}
		
		xButton.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent event) {
				if (event.getSource() == xButton && stylers != null) {
					stylers.dispose();
				}
			}
		});
	}

	protected void applyBoldX(StyledText xButton) {
		if (xButton != null && !xButton.isDisposed()) {
			Styler styler = stylers.boldColoured(SWT.COLOR_DARK_RED);
			StyledString text = new StyledString(X_LABEL, styler);
			xButton.setText(text.getString());
			xButton.setStyleRanges(text.getStyleRanges());
		}
	}

	protected void applyDefaultX(StyledText xButton) {
		if (xButton != null && !xButton.isDisposed()) {
			Styler styler = stylers.darkGrey();
			StyledString text = new StyledString(X_LABEL, styler);
			xButton.setText(text.getString());
			xButton.setStyleRanges(text.getStyleRanges());
		}
	}

	protected void removeSelection() {
		model.getSelection().setValue(false);
	}

	@Override
	public void dispose() {
		if (buttonComp != null && !buttonComp.isDisposed()) {
			buttonComp.dispose();
			buttonComp = null;
		}
		if (selectionListener != null) {
			model.getSelection().removeListener(selectionListener);
		}
	}

	/**
	 * Apply filter and return whether this widget's visibility has changed as a
	 * result.
	 * 
	 * @return Whether visibility of this widget changed.
	 */
	public boolean applyFilter(Filter<T> filter) {
		boolean wasVisible = isVisible.getValue();
		isVisible.setValue(filter.accept(model.getValue()));
		boolean changed = wasVisible != isVisible.getValue();
		return changed;
	}

	public boolean isVisible() {
		return isVisible.getValue();
	}

	@Override
	public String toString() {
		return "SelectedButtonSection(" + model.getLabel() + ")";
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return Validator.OK;
	}
}