/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class InterfaceTableContentProvider implements ITreeContentProvider {

	private final IFile file;

	private final IDOMDocument document;

	public InterfaceTableContentProvider(IFile file, IDOMDocument document) {
		this.file = file;
		this.document = document;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object parentElement) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] getElements(Object inputElement) {
		ArrayList<IType> types = new ArrayList<IType>();
		if (inputElement instanceof Text) {
			String ref = ((Text) inputElement).getText();
			String className = BeansEditorUtils.getClassNameForBean(file, document, ref);
			IType type = JdtUtils.getJavaType(file.getProject(), className);
			if (type != null) {
				types.add(type);
				try {
					ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(type);
					IType[] interfaces = hierarchy.getAllSuperInterfaces(type);
					types.addAll(Arrays.asList(interfaces));
				}
				catch (JavaModelException e) {
					StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID,
							Messages.getString("InterfaceTableContentProvider.ERROR_CONTENT_PROVIDER_DATA"), e)); //$NON-NLS-1$
				}
			}
		}
		return types.toArray();
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}
}
