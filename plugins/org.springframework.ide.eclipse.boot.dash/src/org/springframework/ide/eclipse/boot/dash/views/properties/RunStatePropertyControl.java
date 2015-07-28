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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;

/**
 * Controls for Run State for the properties section
 *
 * @author Alex Boyko
 *
 */
public class RunStatePropertyControl extends AbstractBdePropertyControl {

	private CLabel runState;

	@Override
	public void createControl(Composite composite, TabbedPropertySheetPage page) {
		page.getWidgetFactory().createLabel(composite, "State:").setLayoutData(GridDataFactory.fillDefaults().create()); //$NON-NLS-1$
		runState = page.getWidgetFactory().createCLabel(composite, ""); //$NON-NLS-1$
		runState.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		runState.setBottomMargin(0);
		runState.setLeftMargin(0);
		runState.setRightMargin(0);
		runState.setTopMargin(0);
	}

	@Override
	public void refreshControl() {
		BootDashElement bde = getBootDashElement();
		runState.setText(getLabelProvider().getText(bde, BootDashColumn.RUN_STATE_ICN));

		UIJob job = new UIJob("Boot App run state animation") {

			private int counter = 0;

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				BootDashElement bde = getBootDashElement();
				if (bde != null && !runState.isDisposed()) {
					Image[] images = getLabelProvider().getImageAnimation(bde, BootDashColumn.RUN_STATE_ICN);
					if (images == null || images.length == 0) {
						runState.setImage(null);
					} else if (images.length == 1) {
						runState.setImage(images[0]);
					} else {
						runState.setImage(images[counter % images.length]);
						counter++;
						schedule(100);
					}
				}
				return Status.OK_STATUS;
			}

		};
		job.setSystem(true);
		job.schedule();
	}

}
