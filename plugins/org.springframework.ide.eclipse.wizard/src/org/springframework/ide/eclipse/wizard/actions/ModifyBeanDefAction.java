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
package org.springframework.ide.eclipse.wizard.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.ui.BeanWizardDialog;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * Action for modifying bean definition
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class ModifyBeanDefAction extends Action implements IObjectActionDelegate {

	private IDOMElement selectedBean;

	private IFile selectedFile;

	private boolean matchAndSelect(String beanValue, String attributeName, NamedNodeMap attributes, Node beanNode) {
		Node attributeNode = attributes.getNamedItem(attributeName);
		if (attributeNode == null) {
			return false;
		}

		if (beanValue != null && beanValue.equals(attributeNode.getNodeValue())) {
			if (beanNode instanceof IDOMElement) {
				selectedBean = (IDOMElement) beanNode;
			}
		}
		return true;
	}

	@Override
	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (shell != null && !shell.isDisposed()) {
			if (selectedBean != null && selectedFile != null) {
				BeanWizardDialog dialog = BeanWizardDialog.createModifyBeanWizardDialog(shell, selectedFile,
						selectedBean);
				dialog.create();
				dialog.setBlockOnOpen(true);
				dialog.open();
			}
			else {
				MessageDialog.openError(shell, "Cannot Modify Bean", "Selection does not contain an editable bean.");
			}
		}
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		selectedBean = null;
		selectedFile = null;

		if (selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			Object[] objects = treeSelection.toArray();
			if (objects != null && objects.length == 1) {
				if (objects[0] instanceof IBean) {
					IBean bean = (IBean) objects[0];
					IResource resource = bean.getElementResource();
					if (resource instanceof IFile) {
						selectedFile = (IFile) resource;
						int startLine = bean.getElementStartLine() - 1;
						int endLine = bean.getElementEndLine();

						IModelManager modelManager = StructuredModelManager.getModelManager();
						DOMModelImpl modelForRead = null;
						try {
							modelForRead = (DOMModelImpl) modelManager.getModelForRead(selectedFile);
							IStructuredDocument document = modelForRead.getStructuredDocument();
							int startOffset = document.getLineOffset(startLine);
							int endOffset = document.getLineOffset(endLine);

							if (startOffset == endOffset) {
								endOffset = document.getLength();
							}

							for (int i = startOffset; i < endOffset; i++) {
								Node node = BeansEditorUtils.getNodeByOffset(document, i);
								String localName = node.getLocalName();
								if (localName != null && localName.equals(BeansSchemaConstants.ELEM_BEAN)) {
									NamedNodeMap attributes = node.getAttributes();

									String beanName = bean.getElementName();
									boolean matched = matchAndSelect(beanName, BeansSchemaConstants.ATTR_ID,
											attributes, node);

									if (selectedBean != null) {
										return;
									}

									if (!matched) {
										String className = bean.getClassName();
										matchAndSelect(className, BeansSchemaConstants.ATTR_CLASS, attributes, node);

										if (selectedBean != null) {
											return;
										}
									}
								}
							}
						}
						catch (IOException e) {
							StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
									"Failed to get XML node from bean selection.", e));
						}
						catch (CoreException e) {
							StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
									"Failed to get XML node from bean selection.", e));
						}
						catch (BadLocationException e) {
							StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
									"Failed to get XML node from bean selection.", e));
						}
						finally {
							if (modelForRead != null) {
								modelForRead.releaseFromRead();
								modelForRead = null;
							}
						}
					}
				}
			}
		}

		selectedFile = null;
		selectedBean = null;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
