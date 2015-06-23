/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelectionSource;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

/**
 * Sections that wraps scrollbar support around another section.
 *
 * @author Kris De Volder
 */
public class ScrollerSection extends PageSection implements Disposable, MultiSelectionSource {

	public ScrollerSection(IPageWithSections owner, IPageSection scolledContent) {
		super(owner);
		this.scolledContent = scolledContent;
	}

	private Scroller scroller;
	private IPageSection scolledContent;
	private MultiSelection<?> selection;

	@Override
	public void dispose() {
		if (scolledContent!=null) {
			if (scolledContent instanceof Disposable) {
				((Disposable) scolledContent).dispose();
			}
			scolledContent = null;
		}
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return scolledContent.getValidator();
	}

	@Override
	public void createContents(Composite page) {
		scroller = new Scroller(page);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scroller);
		Composite body = scroller.getBody();
		body.setLayout(GridLayoutFactory.swtDefaults().create());
		scolledContent.createContents(body);
	}

	@Override
	public synchronized MultiSelection<?> getSelection() {
		if (selection==null) {
			if (scolledContent instanceof MultiSelectionSource) {
				selection = ((MultiSelectionSource) scolledContent).getSelection();
			} else {
				selection = MultiSelection.empty(Object.class);
			}
		}
		return selection;
	}

}
