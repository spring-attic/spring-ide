/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.ui.SpringLabelDecorator;
import org.springframework.ide.eclipse.ui.SpringUIImages;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * This decorator adds an overlay image to all Spring beans config files and
 * their corresponding folders and bean classes (Java source and class files).
 * This decoration is refreshed on every modification to the Spring Beans model.
 * Therefore the decorator adds a {@link IModelChangeListener change listener}
 * to the beans model.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansModelLabelDecorator extends LabelProvider implements
		ILightweightLabelDecorator {

	public static final String DECORATOR_ID = BeansUIPlugin.PLUGIN_ID
			+ ".model.beansModelLabelDecorator";

	public static final void update() {
		SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.getDecoratorManager().update(DECORATOR_ID);
				workbench.getDecoratorManager().update(
						SpringLabelDecorator.DECORATOR_ID);
			}
		});
	}

	private IModelChangeListener listener;

	public BeansModelLabelDecorator() {
		listener = new IModelChangeListener() {
			public void elementChanged(ModelChangeEvent event) {
				if ((event.getElement() instanceof IBeansProject || event
						.getElement() instanceof IBeansConfig)
						&& event.getType() != ModelChangeEvent.Type.REMOVED) {
					update();
				}
			}
		};
		BeansCorePlugin.getModel().addChangeListener(listener);
	}

	private void addErrorOverlay(IDecoration decoration, int severity) {
		if (severity == IMarker.SEVERITY_WARNING) {
			decoration.addOverlay(SpringUIImages.DESC_OVR_WARNING,
					IDecoration.BOTTOM_LEFT);
		}
		else if (severity == IMarker.SEVERITY_ERROR) {
			decoration.addOverlay(SpringUIImages.DESC_OVR_ERROR,
					IDecoration.BOTTOM_LEFT);
		}
	}

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFolder) {
			decorateFolder((IFolder) element, decoration);
		}
		else if (element instanceof IFile) {
			decorateFile((IFile) element, decoration);
		}
		else if (element instanceof IJavaElement) {
			decorateJavaElement(((IJavaElement) element), decoration);
		}
		else if (element instanceof IBeansModelElement) {
			decorateBeansModelElement(((IBeansModelElement) element),
					decoration);
		}
	}

	/**
	 * Adds error and warning decorations to {@link IBeansModelElement}.
	 * @since 2.1
	 */
	private void decorateBeansModelElement(IBeansModelElement element,
			IDecoration decoration) {
		addErrorOverlay(decoration, getSeverity(element));
	}

	protected void decorateFile(IFile file, IDecoration decoration) {
		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject project = model.getProject(file.getProject());
		if (project != null) {
			for (IBeansConfig config : project.getConfigs()) {

				// The following comparison works for archived config files too
				if (config.getElementResource().equals(file)) {
					addErrorOverlay(decoration, getSeverity(config));
					decoration.addOverlay(BeansUIImages.DESC_OVR_SPRING);
					break;
				}
			}
		}
	}

	protected void decorateFolder(IFolder folder, IDecoration decoration) {
		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject project = model.getProject(folder.getProject());
		if (project != null) {
			String path = folder.getProjectRelativePath().toString() + '/';
			for (IBeansConfig config : project.getConfigs()) {
				if (config.getElementName().startsWith(path)) {
					decoration.addOverlay(BeansUIImages.DESC_OVR_SPRING);
					break;
				}
			}
		}
	}

	protected void decorateJavaElement(IJavaElement element,
			IDecoration decoration) {
		int type = element.getElementType();
		if (type == IJavaElement.PACKAGE_FRAGMENT_ROOT
				|| type == IJavaElement.CLASS_FILE
				|| type == IJavaElement.COMPILATION_UNIT) {
			IBeansModel model = BeansCorePlugin.getModel();
			IBeansProject project = model.getProject(element.getJavaProject()
					.getProject());
			if (project != null) {
				try {
					if (type == IJavaElement.PACKAGE_FRAGMENT_ROOT) {

						// Decorate JAR file
						IResource resource = ((IPackageFragmentRoot) element)
								.getResource();
						if (resource instanceof IFile) {
							for (IBeansConfig config : project.getConfigs()) {
								if (config.getElementResource()
										.equals(resource)) {
									decoration
											.addOverlay(BeansUIImages.DESC_OVR_SPRING);
									break;
								}
							}
						}
					}
					else if (type == IJavaElement.CLASS_FILE) {

						// Decorate Java class file
						IType javaType = ((IClassFile) element).getType();
						if (BeansModelUtils.isBeanClass(javaType
								.getFullyQualifiedName())) {
							decoration
									.addOverlay(BeansUIImages.DESC_OVR_SPRING);
						}
					}
					else if (type == IJavaElement.COMPILATION_UNIT) {

						// Decorate Java source file
						for (IType javaType : ((ICompilationUnit) element)
								.getTypes()) {
							if (BeansModelUtils.isBeanClass(javaType
									.getFullyQualifiedName())) {
								decoration
										.addOverlay(BeansUIImages.DESC_OVR_SPRING);
								break;
							}
						}
					}
				}
				catch (JavaModelException e) {
					// Ignore
				}
			}
		}
	}

	@Override
	public void dispose() {
		BeansCorePlugin.getModel().removeChangeListener(listener);
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}
	
	protected int getSeverity(Object element) {
		int severity = 0;
		if (element instanceof ISourceModelElement) {
			ISourceModelElement source = (ISourceModelElement) element;
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(source
					.getElementResource(), source.getElementStartLine(), source
					.getElementEndLine());
		}
		else if (element instanceof IResourceModelElement) {
			if (element instanceof IBeansProject) {
				for (IBeansConfig config : ((IBeansProject) element)
						.getConfigs()) {
					severity = MarkerUtils
							.getHighestSeverityFromMarkersInRange(config
									.getElementResource(), -1, -1);
					if (severity == IMarker.SEVERITY_ERROR) {
						break;
					}
				}
			}
			else if (element instanceof IBeansConfigSet) {
				for (IBeansConfig config : ((IBeansConfigSet) element)
						.getConfigs()) {
					severity = MarkerUtils
							.getHighestSeverityFromMarkersInRange(config
									.getElementResource(), -1, -1);
					if (severity == IMarker.SEVERITY_ERROR) {
						break;
					}
				}
			}
			else {
				severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
						((IResourceModelElement) element).getElementResource(),
						-1, -1);
			}
		}
		else if (element instanceof IResource) {
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
					(IResource) element, -1, -1);
		}
		else if (element instanceof ZipEntryStorage) {
			IResource resource = ((ZipEntryStorage) element).getFile();
			severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
					resource, -1, -1);
		}
		return severity;
	}
}
