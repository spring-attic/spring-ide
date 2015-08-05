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

import static org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn.RUN_STATE_ICN;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.TagUtils;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.ui.ImageDescriptorRegistry;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Provides various methods for implementing various Label providers for the Boot Dash
 * and its related views, dialogs etc.
 * <p>
 * This is meant to be used as a 'delegate' object that different label provider
 * implementations can wrap and use rather than a direct implementation of
 * a particular label provider interface.
 * <p>
 * Instances of this class may allocate resources (e.g. images)
 * and must be disposed when they are not needed anymore.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class BootDashLabels implements Disposable {

	private static final String UNKNOWN_LABEL = "???";

	private static final Image[] NO_IMAGES = null;

	private AppearanceAwareLabelProvider javaLabels = null;
	private RunStateImages runStateImages = null;

	/**
	 * TODO replace 'runStateImages' and this registry with a single registry
	 * for working with both animatons & simple images.
	 */
	private ImageDescriptorRegistry images;

	private Stylers stylers;

	/**
	 * This constructor is deprecated. It produces something incapable of
	 * properly styling some kinds of labels (e.g. those requiring the use
	 * of a 'bold' font. Use the alternate constructor which
	 * takes a {@link Stylers} argument.
	 */
	@Deprecated
	public BootDashLabels() {
		//Create slighly less-capable 'Stylers':
		this(new Stylers(null));
	}

	public BootDashLabels(Stylers stylers) {
		this.stylers = stylers;
	}

	///////////////////////////////////////////////////////////////
	//// Main apis that clients should use:

	@Override
	public void dispose() {
		if (javaLabels != null) {
			javaLabels.dispose();
		}
		if (runStateImages!=null) {
			runStateImages.dispose();
			runStateImages = null;
		}
		if (images!=null) {
			images.dispose();
			images = null;
		}
	}

	public Image[] getImageAnimation(Object e, BootDashColumn forColum) {
		if (e instanceof BootDashElement) {
			return getImageAnimation((BootDashElement)e, forColum);
		} else if (e instanceof BootDashModel) {
			return getImageAnimation((BootDashModel)e, forColum);
		}
		return NO_IMAGES;
	}

	/**
	 * For those who don't care about animations, fetches the first image
	 * of the animation sequence only; or if the icon is non-animated just
	 * the image.
	 */
	public final Image getImage(Object element, BootDashColumn column) {
		Image[] imgs = getImageAnimation(element, column);
		if (imgs!=null && imgs.length>0) {
			return imgs[0];
		}
		return null;
	}

	public StyledString getStyledText(Object element, BootDashColumn column) {
		if (element instanceof BootDashElement) {
			return getStyledText((BootDashElement)element, column);
		} else if (element instanceof BootDashModel) {
			return getStyledText((BootDashModel)element, column);
		}
		return new StyledString(""+element);
	}

	///////////////////////////////////////////////////
	// Type-specific apis below
	//
	// Some label providers may be only for specific types of elements and can use these
	// methods instead.

	public Image[] getImageAnimation(BootDashModel element, BootDashColumn column) {
		return toAnimation(element.getRunTarget().getType().getIcon());
	}

	private Image[] toAnimation(ImageDescriptor icon) {
		if (images==null) {
			images = new ImageDescriptorRegistry();
		}
		Image img = images.get(icon);
		if (img!=null) {
			return new Image[]{img};
		}
		return NO_IMAGES;
	}

	public Image[] getImageAnimation(BootDashElement element, BootDashColumn column) {
		if (element != null) {
			switch (column) {
			case PROJECT:
				IJavaProject jp = element.getJavaProject();
				return jp == null ? new Image[0] : new Image[] { getJavaLabels().getImage(jp) };
			case TREE_VIEWER_MAIN:
			case RUN_STATE_ICN:
				return getRunStateAnimation(element.getRunState());
			default:
				return NO_IMAGES;
			}
		}
		return NO_IMAGES;
	}

	public StyledString getStyledText(BootDashModel element, BootDashColumn column) {
		if (element!=null) {
			return getStyledText(element.getRunTarget(), column);
		}
		return stylers==null?new StyledString("null"):new StyledString("null", stylers.red());
	}

	public StyledString getStyledText(RunTarget element, BootDashColumn column) {
		//TODO: We don't care about columns (yet?)
		if (element!=null) {
			//TODO: prettier labels ? Each target type could specify a way to render its target's labels more
			// colorfully.
			return new StyledString(element.getName());
		}
		return new StyledString(UNKNOWN_LABEL);
	}

	public StyledString getStyledText(BootDashElement element, BootDashColumn column) {
		//The big case below should set either one of 'label' or'styledLabel', depending
		// on whether it is 'styling capable'.
		String label = null;
		StyledString styledLabel = null;

		if (element != null) {
			switch(column) {
			case TAGS:
				String text = TagUtils.toString(element.getTags());
				styledLabel = stylers == null ? new StyledString(text) : TagUtils.applyTagStyles(text, stylers.tag());
				break;
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
					styledLabel = getJavaLabels().getStyledText(jp);
				}
				break;
			case HOST:
				String host = element.getLiveHost();
				label = host == null ? UNKNOWN_LABEL : host;
				break;
			case TREE_VIEWER_MAIN:
				BootDashColumn[] cols = element.getParent().getRunTarget().getDefaultColumns();
				styledLabel = new StyledString();
				boolean first = true;
				for (BootDashColumn col : cols) {
					//Ignore RUN_STATE_ICN because its already represented in the label's icon.
					if (col!=RUN_STATE_ICN) {
						if (!first) {
							styledLabel = styledLabel.append(" ");
						}
						styledLabel = styledLabel.append(getStyledText(element, col));
						first = false;
					}
				}
				break;
			case APP:
				String app = element.getName();
				label = app == null ? UNKNOWN_LABEL : app;
				break;
			case RUN_STATE_ICN:
				label = element.getRunState().toString();
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
		}
		if (styledLabel!=null) {
			return styledLabel;
		} else if (label!=null) {
			return new StyledString(label);
		}
		return new StyledString(UNKNOWN_LABEL);
	}

	/**
	 * Deprecated: use getStyledText.
	 */
	@Deprecated
	public String getText(BootDashElement element, BootDashColumn column) {
		return getStyledText(element, column).getString();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	// private / helper stuff

	private AppearanceAwareLabelProvider getJavaLabels() {
		if (javaLabels == null) {
			javaLabels = new AppearanceAwareLabelProvider();
		}
		return javaLabels;
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
