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
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.progress.UIJob;

/**
 * A TableViewerAnimator manages a Job that periodically updates labels for
 * 'animated' label icons in a TableViewwer.
 *
 * @author Kris De Volder
 */
public class TableViewerAnimator {

	public class CellAnimation {
		public final int col;
		public final Image[] imgs;
		public final TableItem item;

		public CellAnimation(ViewerCell cell, Image[] imgs) {
			this.item = (TableItem)cell.getViewerRow().getItem();
			this.col = cell.getColumnIndex();
			this.imgs = imgs;
		}

	}

	protected static final long INTERVAL = 100;

	private int animationCounter = 0;

	private TableViewer tv;

	public TableViewerAnimator(TableViewer tv) {
		this.tv = tv;
	}

	private Map<ViewerCell, CellAnimation> animatedElements = new HashMap<ViewerCell, CellAnimation>();

	private Job job;

	public void setAnimation(ViewerCell cell, Image[] images) {
		if (images==null) {
			cell.setImage(null);
			stopAnimation(cell);
		} else if (images.length==1) {
			stopAnimation(cell);
			cell.setImage(images[0]);
		} else {
			cell.setImage(currentImage(images));
			startAnimation(cell, images);
		}
	}

	private synchronized void stopAnimation(Object e) {
		animatedElements.remove(e);
	}

	private synchronized void startAnimation(ViewerCell cell, Image[] imgs) {
		animatedElements.put(cell, new CellAnimation(cell, imgs));
		ensureJob();
		job.schedule();
	}

	private void ensureJob() {
		if (job==null) {
			job = new UIJob("Animate table icons") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (!tv.getTable().isDisposed()) {
						animationCounter++;
						for (CellAnimation a : animatedElements.values()) {
							Image[] imgs = a.imgs;
							a.item.setImage(a.col, imgs[animationCounter%imgs.length]);
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
	}

	private Image currentImage(Image[] images) {
		return images[animationCounter%images.length];
	}

	public void dispose() {
		job = null;
	}

}