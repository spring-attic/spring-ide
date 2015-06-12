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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PageSection;

/**
 * Section containing an ExpandableComposite that contains another
 * section which is shown/hidden inside the expandable composite.
 * <p>
 * Note: This has not been used in many contexts and may not
 * be re-usable as is in contexts where it hasn't been tested.
 * The component is somewhat fiddly w.r.t. how parent composite
 * need to reflow their layout when this element is
 * expanded/collapsed.
 *
 * @author Kris De Volder
 */
public class ExpandableSection extends PageSection {

	private IPageSection child;
	private String title;

	public ExpandableSection(IPageWithSections owner, String title, IPageSection expandableContent) {
		super(owner);
		this.title = title;
		this.child = expandableContent;
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return OK_VALIDATOR;
	}

	@Override
	public void createContents(final Composite page) {
		final ExpandableComposite comp = new ExpandableComposite(page, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comp);
		comp.setExpanded(true);
		comp.setText(title);
		comp.setLayout(new FillLayout());
		comp.addExpansionListener(new IExpansionListener() {
			public void expansionStateChanging(ExpansionEvent e) {
			}

			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				page.layout(new Control[]{comp});
			}
		});
		Composite client = new Composite(comp, SWT.NONE);
		client.setLayout(new GridLayout());
		child.createContents(client);
		comp.setClient(client);
	}

}
