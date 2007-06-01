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
package org.springframework.ide.eclipse.aop.core.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceElementFactory implements IElementFactory {

	public static String FACTORY_ID = Activator.PLUGIN_ID
			+ ".aopReferenceElementFactory";

	public final IAdaptable createElement(IMemento memento) {
		ADVICE_TYPES type = ADVICE_TYPES.valueOf(memento
				.getString("advice-type"));
		String sourceHandle = memento.getString("source");
		IJavaElement source = null;
		if (sourceHandle != null) {
			source = JavaCore.create(sourceHandle);
		}
		String targetHandle = memento.getString("target");
		IJavaElement target = null;
		if (targetHandle != null) {
			target = JavaCore.create(targetHandle);
		}
		String fileName = memento.getString("file");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(fileName);

		String beanId = memento.getString("bean");
		if (member != null && member instanceof IFile && source != null
				&& target != null && source instanceof IMember
				&& target instanceof IMember) {
			return new AopReference(type, (IMember) source, (IMember) target,
					member, beanId);
		}
		return null;
	}
}
