/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
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
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceElementFactory implements IElementFactory {

	protected static final String BEAN_ATTRIBUTE = "bean";

	protected static final String BEAN_FILE_ATTRIBUTE = "bean-file";

	protected static final String BEAN_START_LINE_ATTRIBUTE = "bean-start-line";

	protected static final String FILE_ATTRIBUTE = "file";

	protected static final String TARGET_ATTRIBUTE = "target";

	protected static final String SOURCE_ATTRIBUTE = "source";

	protected static final String ADVICE_TYPE_ATTRIBUTE = "advice-type";

	public static final String SOURCE_START_LINE_ATTRIBUTE = "source-start-line";

	public static final String TARGET_START_LINE_ATTRIBUTE = "target-start-line";

	public static String FACTORY_ID = Activator.PLUGIN_ID + ".aopReferenceElementFactory";

	public final IAdaptable createElement(IMemento memento) {
		ADVICE_TYPE type = ADVICE_TYPE.valueOf(memento.getString(ADVICE_TYPE_ATTRIBUTE));
		
		String sourceHandle = memento.getString(SOURCE_ATTRIBUTE);
		Integer sourceStartLine = Integer.valueOf(-1);
		IJavaElement source = null;
		if (sourceHandle != null) {
			source = JavaCore.create(sourceHandle);
			sourceStartLine = memento.getInteger(SOURCE_START_LINE_ATTRIBUTE);
			if (sourceStartLine == null) {
				sourceStartLine = JdtUtils.getLineNumber(source);
			}
		}

		String targetHandle = memento.getString(TARGET_ATTRIBUTE);
		Integer targetStartLine = Integer.valueOf(-1);
		IJavaElement target = null;
		if (targetHandle != null) {
			target = JavaCore.create(targetHandle);
			targetStartLine = memento.getInteger(TARGET_START_LINE_ATTRIBUTE);
			if (targetStartLine == null) {
				targetStartLine = JdtUtils.getLineNumber(target);
			}
		}

		String fileName = memento.getString(FILE_ATTRIBUTE);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource member = root.findMember(fileName);

		String beanId = memento.getString(BEAN_ATTRIBUTE);
		Integer beanStartline = memento.getInteger(BEAN_START_LINE_ATTRIBUTE);
		if (beanStartline == null) {
			beanStartline = 0;
		}

		String beanFileName = memento.getString(BEAN_FILE_ATTRIBUTE);
		IResource beanResource = null;
		// Pre 2.3.0 version weren't persisting the file attribute; so be careful
		if (beanFileName != null) {
			beanResource = root.findMember(beanFileName);
		}

		if (member != null && member instanceof IFile && source != null && target != null && source instanceof IMember
				&& target instanceof IMember && beanResource != null && beanResource instanceof IFile) {
			return new AopReference(type, (IMember) source, sourceStartLine, (IMember) target, targetStartLine, member,
					beanId, beanResource, beanStartline);
		}
		return null;
	}
}
