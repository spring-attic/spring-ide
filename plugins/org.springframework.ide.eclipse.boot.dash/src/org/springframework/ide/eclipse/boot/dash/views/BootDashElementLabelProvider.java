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
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.dash.views.sections.UIUtils;

/**
 * Label provider for {@link BootDashElement} and its properties
 *
 * @author Alex Boyko
 *
 */
@SuppressWarnings("restriction")
public class BootDashElementLabelProvider implements ILabelProvider, IStyledLabelProvider {

	private static final String UNKNOWN_LABEL = "???";

	private AppearanceAwareLabelProvider javaLabels = null;
	private RunStateImages runStateImages = null;
	private List<ILabelProviderListener> listeners;

	public BootDashElementLabelProvider() {
		this.listeners = new ArrayList<ILabelProviderListener>();
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	@Override
	public void dispose() {
		if (javaLabels != null) {
			javaLabels.dispose();
		}
		if (runStateImages!=null) {
			runStateImages.dispose();
			runStateImages = null;
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	public StyledString getStyledText(BootDashElement element, BootDashColumn column, Stylers stylers) {
		StyledString label = new StyledString();
		if (element == null) {
			return label;
		}
		switch(column) {
		case TAGS:
			String text = getText(element, column);
			label = stylers == null ? new StyledString(text) : UIUtils.applyTagStyles(text, stylers.tag());
			break;
		default:
			label = new StyledString(getText(element, column));
		}
		return label;
	}

	@Override
	public StyledString getStyledText(Object element) {
		return new StyledString(getText(element));
	}

	public Image getImage(BootDashElement element, BootDashColumn column) {
		Image image = null;
		if (element == null) {
			return image;
		}
		switch (column) {
		case PROJECT:
			IJavaProject jp = element.getJavaProject();
			image = jp == null ? null : getJavaLabels().getImage(jp);
			break;
		case RUN_STATE_ICN:
			image = getRunStateImage(element.getRunState());
			break;
		default:
			image = null;
		}
		return image;
	}

	public Image[] getImageAnimation(BootDashElement element, BootDashColumn column) {
		Image[] image = new Image[0];
		if (element == null) {
			return image;
		}
		switch (column) {
		case PROJECT:
			IJavaProject jp = element.getJavaProject();
			image = jp == null ? new Image[0] : new Image[] { getJavaLabels().getImage(jp) };
			break;
		case RUN_STATE_ICN:
			image = getRunStateAnimation(element.getRunState());
			break;
		default:
			image = new Image[0];
		}
		return image;
	}

	@Override
	public Image getImage(Object element) {
		return null;
	}

	public String getText(BootDashElement element, BootDashColumn column) {
		String label = "";
		if (element == null) {
			return label;
		}
		switch(column) {
		case PROJECT:
			IJavaProject jp = element.getJavaProject();
			if (jp == null) {
				// Not all projects in elements are Java projects. CF elements accept any project that contains a valid manifest.yml since the manifest.yml may
				// point to an executable archive for the app (.jar/.war)
				IProject project = element.getProject();
				if (project != null) {
					label = project.getName();
				} else {
					// Project and app (element) name are shown in separate columns now. If
					// there is no project mapping
					// do not show the element name anymore. That way the user knows that there is
					// no mapping for that element.
					label = "";
				}
			} else {
				label = getJavaLabels().getText(jp);
			}
			break;
		case HOST:
			String host = element.getLiveHost();
			label = host == null ? UNKNOWN_LABEL : host;
			break;
		case APP:
			String app = element.getName();
			label = app == null ? UNKNOWN_LABEL : app;
			break;
		case RUN_STATE_ICN:
			label = element.getRunState().toString();
			break;
		case TAGS:
			label = TagUtils.toString(element.getTags());
			break;
		case LIVE_PORT:
			int port = element.getLivePort();
			label = port < 0 ? UNKNOWN_LABEL : String.valueOf(port);
			break;
		case DEFAULT_PATH:
			String path = element.getDefaultRequestMappingPath();
			label = path == null ? "" : path;
			break;
		case INSTANCES:
			int actual = element.getActualInstances();
			int desired = element.getDesiredInstances();
			label = actual + "/" + desired;
			break;
		default:
			label = UNKNOWN_LABEL;
		}
		return label;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof BootDashElement) {
			return ((BootDashElement)element).getName();
		}
		return null;
	}

	private AppearanceAwareLabelProvider getJavaLabels() {
		if (javaLabels == null) {
			javaLabels = new AppearanceAwareLabelProvider();
		}
		return javaLabels;
	}

	private Image getRunStateImage(RunState runState) {
		try {
			if (runStateImages==null) {
				runStateImages = new RunStateImages();
			}
			return runStateImages.getAnimation(runState)[0];
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

	private Image[] getRunStateAnimation(RunState runState) {
		try {
			if (runStateImages==null) {
				runStateImages = new RunStateImages();
			}
			return runStateImages.getAnimation(runState);
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}

}
