/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel.ElementStateListener;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

/**
 * Abstract super class for BootDash actions that operate on selections
 * of elements.
 *
 * @author Kris De Volder
 */
public class AbstractBootDashElementsAction extends AbstractBootDashAction {

	private final MultiSelection<BootDashElement> selection;
	private ValueListener<ImmutableSet<BootDashElement>> selectionListener;
	protected final BootDashViewModel model;
	private ElementStateListener modelListener;

	public AbstractBootDashElementsAction(MultiSelection<BootDashElement> selection, UserInteractions ui) {
		this(null, selection, ui);
	}

	public AbstractBootDashElementsAction(BootDashViewModel model, MultiSelection<BootDashElement> _selection, UserInteractions ui) {
		super(ui);
		this.model = model;
		this.selection = _selection;
		if (model!=null) {
			model.addElementStateListener(modelListener = new ElementStateListener() {
				public void stateChanged(BootDashElement e) {
					if (selection.getValue().contains(e)) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								updateEnablement();
							}
						});
					}
				}
			});
		}
		selection.getElements().addListener(selectionListener = new ValueListener<ImmutableSet<BootDashElement>>() {
			public void gotValue(LiveExpression<ImmutableSet<BootDashElement>> exp, ImmutableSet<BootDashElement> selecteds) {
				updateEnablement();
			}
		});
	}

	/**
	 * Subclass can override to compuet enablement differently.
	 * The default implementation enables if a single element is selected.
	 */
	public void updateEnablement() {
		Collection<BootDashElement> selecteds = getSelectedElements();
		this.setEnabled(selecteds.size()==1);
	}

	public Collection<BootDashElement> getSelectedElements() {
		return selection.getValue();
	}

	protected BootDashElement getSingleSelectedElement() {
		return selection.getSingle();
	}

	public void dispose() {
		if (selectionListener!=null) {
			selection.getElements().removeListener(selectionListener);
		}
		if (modelListener!=null) {
			model.removeElementStateListener(modelListener);
			modelListener = null;
		}
		super.dispose();
	}
}
