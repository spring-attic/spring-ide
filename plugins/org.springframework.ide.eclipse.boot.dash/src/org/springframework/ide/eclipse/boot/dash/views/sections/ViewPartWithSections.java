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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.ui.Reflowable;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions.UIContext;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.ValidatorSection;

public class ViewPartWithSections extends ViewPart implements UIContext, IPageWithSections, Reflowable {

	private final boolean enableScrolling;
	private Scroller scroller;
	protected Composite page;

	public ViewPartWithSections(boolean enableScrolling) {
		super();
		this.enableScrolling = enableScrolling;
	}

	@Override
	public void createPartControl(Composite parent) {
		if (enableScrolling) {
			scroller = new Scroller(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			page = scroller.getBody();
		} else {
			page = new Composite(parent, SWT.NONE);
		}
		page.setLayout(new GridLayout());

		for (IPageSection s : getSections()) {
			s.createContents(page);
		}

		Display display = page.getDisplay();
		Color background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		stylePage(new Control[]{page}, background);
	}

	protected void stylePage(Control[] controls, Color backgroundColor) {
		if (controls == null || controls.length == 0) {
			return;
		}
		for (Control control : controls) {
			if (control instanceof Composite) {
				control.setBackground(backgroundColor);
				Control[] children = ((Composite)control).getChildren();
				stylePage(children, backgroundColor);
			}
		}
	}

	@Override
	public void setFocus() {
		page.setFocus();
	}

	////////////////////////////////////////////////////////////////////////

	@Override
	public Shell getShell() {
		return getSite().getShell();
	}

	@Override
	public IRunnableContext getRunnableContext() {
		return getSite().getWorkbenchWindow();
	}

	////////////////////////////////////////////////////////////////////////
	/// Sections... lazy creation etc.

	private List<IPageSection> sections;

	protected synchronized List<IPageSection> getSections() {
		if (sections==null) {
			sections = safeCreateSections();
		}
		return sections;
	}

	private List<IPageSection> safeCreateSections() {
		try {
			return createSections();
		} catch (CoreException e) {
			BootActivator.log(e);
			return Arrays.asList(new IPageSection[] {
					new CommentSection(this, "View contents couldn't be created because of an unexpected error:+\n"+ExceptionUtil.getMessage(e)+"\n\n"
							+ "Check the error log for details"),
					new ValidatorSection(Validator.alwaysError(ExceptionUtil.getMessage(e)), this)
			});
		}
	}

	/**
	 * This method should be implemented to generate the contents of the page.
	 */
	protected List<IPageSection> createSections() throws CoreException {
		//This default implementation is meant to be overridden
		return Arrays.asList(new IPageSection[]{
				new CommentSection(this, "Override ViewPartWithSections.createSections() to provide real content."),
				new ValidatorSection(Validator.alwaysError("Subclass must implement validation logic"), this)
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		if (sections!=null) {
			for (IPageSection s : sections) {
				if (s instanceof Disposable) {
					((Disposable) s).dispose();
				}
			}
			sections = null;
		}
	}

	@Override
	public boolean reflow() {
		if (enableScrolling) {
			if (scroller!=null && !scroller.isDisposed()) {
				scroller.reflow(true);
			}
		} else {
			if (page!=null && !page.isDisposed()) {
				page.layout();
			}
		}
		return true;
	}

}
