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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

public class WizardCompositeSection extends WizardPageSection {
	private CompositeValidator validator;
	private int columns = 1; // one column by default
	protected List<WizardPageSection> sections;
	public final LiveVariable<Boolean> isVisible = new LiveVariable<Boolean>(true);

	private boolean equalWidthColumns = true;

	public WizardCompositeSection(IPageWithSections owner, WizardPageSection... _sections) {
		super(owner);
		this.sections = new ArrayList<WizardPageSection>();
		for (WizardPageSection s : _sections) {
			if (s != null) {
				sections.add(s);
			}
		}

		validator = new CompositeValidator();
		for (WizardPageSection s : sections) {
			validator.addChild(s.getValidator());
		}
	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	@Override
	public void createContents(Composite page) {
		final Composite area = createComposite(page);
		for (WizardPageSection s : sections) {
			s.createContents(area);
		}
		isVisible.addListener(new ValueListener<Boolean>() {
			public void gotValue(LiveExpression<Boolean> exp, Boolean isVisible) {
				area.setVisible(isVisible);
				GridData layout = (GridData) area.getLayoutData();
				layout.exclude = !isVisible;
				area.setLayoutData(layout);
				Shell shell = owner.getShell();
				if (shell != null) {
					shell.layout(new Control[] { area });
				}
			};
		});
	}

	protected void applyLayout(Composite composite) {
		GridLayoutFactory.fillDefaults().numColumns(columns).equalWidth(equalWidthColumns).applyTo(composite);
	}

	protected void applyLayoutData(Composite composite) {
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
	}

	@Override
	public void dispose() {
		for (WizardPageSection s : sections) {
			s.dispose();
		}
		super.dispose();
	}

	protected Composite createComposite(Composite page) {
		Composite area = new Composite(page, SWT.NONE);

		applyLayout(area);
		applyLayoutData(area);
		return area;
	}

}
