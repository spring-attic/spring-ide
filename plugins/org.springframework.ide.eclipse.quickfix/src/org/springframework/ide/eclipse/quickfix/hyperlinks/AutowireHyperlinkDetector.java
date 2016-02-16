/*******************************************************************************
 *  Copyright (c) 2013, 2015 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Softare, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.hyperlinks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * @since 3.3.0
 */
public class AutowireHyperlinkDetector extends JavaElementHyperlinkDetector {

	@Override
	protected void addHyperlinks(List<IHyperlink> hyperlinksCollector, final IRegion wordRegion,
			SelectionDispatchAction openAction, IJavaElement element, boolean qualify, JavaEditor editor) {

		if (element instanceof ILocalVariable) {
			ILocalVariable localVariable = (ILocalVariable) element;
			IJavaElement parent = localVariable.getParent();
			if (parent instanceof IMethod) {
				IMethod parentMethod = (IMethod) parent;
				if (parentMethod.getAnnotation("Autowired") != null) {
					String typeSignature = localVariable.getTypeSignature();
					addHyperlinksHelper(typeSignature, localVariable, hyperlinksCollector);
				}
			}
		}
		else if (element instanceof IField) {
			IField field = (IField) element;
			if (field.getAnnotation("Autowired") != null) {
				try {
					String typeSignature = field.getTypeSignature();
					addHyperlinksHelper(typeSignature, field, hyperlinksCollector);
				}
				catch (JavaModelException e) {
					StatusHandler.log(e.getStatus());
				}
			}
		}
	}

	private void addHyperlinksHelper(String typeSignature, IJavaElement element, List<IHyperlink> hyperlinksCollector) {
		String typeName = Signature.toString(typeSignature);
		try {
			IType type = getParentType(element);
			if (type != null) {
				String[][] qualifiedTypeNames = type.resolveType(typeName);
				if (qualifiedTypeNames != null) {
					for (String[] typeNameSegments : qualifiedTypeNames) {
						StringBuilder qualifiedTypeName = new StringBuilder();
						for (String typeNameSegment : typeNameSegments) {
							if (qualifiedTypeName.length() > 0) {
								qualifiedTypeName.append(".");
							}
							qualifiedTypeName.append(typeNameSegment);
						}

						addHyperlinksHelper(qualifiedTypeName.toString(), element.getJavaProject().getProject(),
								hyperlinksCollector);
					}
				}
			}
		}
		catch (JavaModelException e) {
			StatusHandler.log(e.getStatus());
		}
	}

	private void addHyperlinksHelper(final String typeName, final IProject project,
			final List<IHyperlink> hyperlinksCollector) {
		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject springProject = model.getProject(project);

		if (springProject == null) {
			return;
		}

		Set<IBeansConfig> configs = springProject.getConfigs();
		Set<AutowireBeanHyperlink> hyperlinks = new HashSet<AutowireBeanHyperlink>();
		for (IBeansConfig config : configs) {
			addHyperlinksHelper(config, typeName, project, hyperlinks);
		}

		if (hyperlinks.size() == 1) {
			hyperlinks.iterator().next().setIsOnlyCandidate(true);
		}
		hyperlinksCollector.addAll(hyperlinks);
	}

	// public for testing
	public void addHyperlinksHelper(IBeansConfig config, final String typeName, final IProject project,
			final Set<AutowireBeanHyperlink> hyperlinks) {
		final AutowireDependencyProvider autowireDependencyProvider = new AutowireDependencyProvider(config, config);
		final String[][] beanNamesWrapper = new String[1][];

		try {
			IProjectClassLoaderSupport classLoaderSupport = JdtUtils.getProjectClassLoaderSupport(project.getProject(),
					null);
			autowireDependencyProvider.setProjectClassLoaderSupport(classLoaderSupport);
			classLoaderSupport.executeCallback(new IProjectClassLoaderAwareCallback() {
				public void doWithActiveProjectClassLoader() throws Throwable {
					autowireDependencyProvider.preloadClasses();
					beanNamesWrapper[0] = autowireDependencyProvider.getBeansForType(typeName);
				}
			});
		}
		catch (Throwable e) {
			BeansCorePlugin.log(e);
		}

		String[] beanNames = beanNamesWrapper[0];
		if (beanNames != null) {
			for (final String beanName : beanNames) {
				IBean bean = autowireDependencyProvider.getBean(beanName);
				final IResource resource = bean.getElementResource();
				final int line = bean.getElementStartLine();
				if (resource instanceof IFile) {
					AutowireBeanHyperlink newHyperlink = new AutowireBeanHyperlink((IFile) resource, line, beanName);
					boolean found = false;

					for (AutowireBeanHyperlink hyperlink : hyperlinks) {
						if (resource.equals(hyperlink.getFile()) && line == hyperlink.getLine()) {
							if (!beanName.equals(hyperlink.getBeanName())) {
								hyperlink.setShowFileName(true);
								hyperlinks.add(newHyperlink);
								newHyperlink.setShowFileName(true);
								break;
							}
							found = true;
						}
					}

					if (!found) {
						hyperlinks.add(newHyperlink);
					}
				}
			}
		}
	}

	private IType getParentType(IJavaElement element) {
		if (element == null) {
			return null;
		}

		if (element instanceof IType) {
			return (IType) element;
		}

		return getParentType(element.getParent());
	}

}
