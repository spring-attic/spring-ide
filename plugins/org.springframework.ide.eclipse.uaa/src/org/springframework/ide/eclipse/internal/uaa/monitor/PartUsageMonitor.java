/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.monitor;

import java.util.Collections;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.uaa.IUaa;

/**
 * Helper class that captures open and activate events for Eclipse Views, Editors and Perspectives.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class PartUsageMonitor implements IUsageMonitor {

	private static final String EDITORS_EXTENSION_POINT = "org.eclipse.ui.editors"; //$NON-NLS-1$

	private static final String PERSPECTIVES_EXTENSION_POINT = "org.eclipse.ui.perspectives"; //$NON-NLS-1$

	private static final String VIEWS_EXTENSION_POINT = "org.eclipse.ui.views"; //$NON-NLS-1$

	private ExtensionIdToBundleMapper editorToBundleIdMapper;

	private IUaa manager;

	private IPageListener pageListener = new IPageListener() {
		public void pageActivated(IWorkbenchPage page) {
		}

		public void pageClosed(IWorkbenchPage page) {
			unhookListeners(page);
		}

		public void pageOpened(IWorkbenchPage page) {
			hookListeners(page);
		}

	};

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			recordEvent(part);
		}

		public void partBroughtToTop(IWorkbenchPart part) {
			// Don't care.
		}

		public void partClosed(IWorkbenchPart part) {
			// Don't care.
		}

		public void partDeactivated(IWorkbenchPart part) {
			// Don't care.
		}

		public void partOpened(IWorkbenchPart part) {
			// Don't care.
		}
	};

	private IPerspectiveListener perspectiveListener = new IPerspectiveListener() {
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			recordEvent(perspective);
		}

		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		}

	};

	private ExtensionIdToBundleMapper perspectiveToBundleIdMapper;

	private ExtensionIdToBundleMapper viewToBundleIdMapper;

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(IUaa manager) {
		this.manager = manager;
		perspectiveToBundleIdMapper = new ExtensionIdToBundleMapper(PERSPECTIVES_EXTENSION_POINT);
		viewToBundleIdMapper = new ExtensionIdToBundleMapper(VIEWS_EXTENSION_POINT);
		editorToBundleIdMapper = new ExtensionIdToBundleMapper(EDITORS_EXTENSION_POINT);
		IWorkbench workbench = PlatformUI.getWorkbench();
		hookListeners(workbench);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stopMonitoring() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		unhookListeners(workbench);
		if (perspectiveToBundleIdMapper != null) {
			perspectiveToBundleIdMapper.dispose();
		}
		if (viewToBundleIdMapper != null) {
			viewToBundleIdMapper.dispose();
		}
		if (editorToBundleIdMapper != null) {
			editorToBundleIdMapper.dispose();
		}
	}

	private void hookListener(IWorkbenchWindow window) {
		if (window == null)
			return;
		window.addPageListener(pageListener);
		window.addPerspectiveListener(perspectiveListener);
		for (IWorkbenchPage page : window.getPages()) {
			hookListeners(page);
		}
	}

	private void hookListeners(final IWorkbench workbench) {
		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			hookListener(window);
		}
	}

	private void hookListeners(IWorkbenchPage page) {
		IPerspectiveDescriptor perspective = page.getPerspective();
		if (perspective != null) {
			recordEvent(perspective);
		}
		page.addPartListener(partListener);
	}

	private void recordEvent(IPerspectiveDescriptor perspective) {
		if (manager != null) {
			manager.registerFeatureUse(perspectiveToBundleIdMapper.getBundleId(perspective.getId()),
					Collections.singletonMap("perspective", perspective.getLabel()));
		}
	}

	private void recordEvent(IWorkbenchPart part) {
		if (manager != null) {
			if (part.getSite() instanceof IViewSite) {
				manager.registerFeatureUse(viewToBundleIdMapper.getBundleId(part.getSite().getId()),
						Collections.singletonMap("view", part.getSite().getRegisteredName()));
			}
			else if (part.getSite() instanceof IEditorSite) {
				manager.registerFeatureUse(editorToBundleIdMapper.getBundleId(part.getSite().getId()),
						Collections.singletonMap("editor", part.getSite().getRegisteredName()));
			}
		}
	}

	private void unhookListeners(final IWorkbench workbench) {
		if (workbench.getDisplay().isDisposed())
			return;

		for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
			unhookListeners(window);
		}
	}

	private void unhookListeners(IWorkbenchPage page) {
		page.removePartListener(partListener);
	}

	private void unhookListeners(IWorkbenchWindow window) {
		if (window == null)
			return;
		window.removePageListener(pageListener);
		window.removePerspectiveListener(perspectiveListener);
		for (IWorkbenchPage page : window.getPages()) {
			unhookListeners(page);
		}
	}

}