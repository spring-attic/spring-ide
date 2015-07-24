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
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.UIJob;

/**
 * A TableViewerAnimator manages a Job that periodically updates labels for
 * 'animated' label icons in a TableViewwer.
 *
 * TODO: This implementation calls refresh for animated table elements which
 * will cause the whole table row to refresh. This is overkill just to
 * replace one image on a single cell in the row. Can we do better?
 *
 * @author Kris De Volder
 */
public class TableViewerAnimator {

	protected static final long INTERVAL = 300;

	private int animationCounter = 0;

	private TableViewer tv;

	public TableViewerAnimator(TableViewer tv) {
		this.tv = tv;
	}

	private Map<Object, Image[]> animatedElements = new HashMap<Object, Image[]>();

	private Job job;

	public void setAnimation(ViewerCell cell, Image[] images) {
		Object e = cell.getElement();
		if (e!=null) {
			if (images==null) {
				cell.setImage(null);
				stopAnimation(e);
			} else if (images.length==1) {
				stopAnimation(e);
				cell.setImage(images[0]);
			} else {
				cell.setImage(currentImage(images));
				startAnimation(e, images);
			}
		}
	}



	private void stopAnimation(Object e) {
		animatedElements.remove(e);
	}

	private synchronized void startAnimation(Object e, Image[] imgs) {
		animatedElements.put(e, imgs);
		if (job==null) {
			job = new UIJob("Animate table icons") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (!tv.getTable().isDisposed()) {
						animationCounter++;
						for (Object e : animatedElements.keySet()) {
							tv.refresh(e);
						}
						if (job!=null && animatedElements.size()>0) {
							job.schedule(INTERVAL);
						}
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
		}
		job.schedule();
	}

	private Image currentImage(Image[] images) {
		return images[animationCounter%images.length];
	}

	public void dispose() {
		job = null;
	}

}