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
package org.springframework.ide.eclipse.quickfix.proposals;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Document;


/**
 * @author Terry Denney
 */
public class AddFactoryMethodQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final IDOMNode beanNode;

	public AddFactoryMethodQuickFixProposal(int offset, int length, boolean missingEndQuote, IDOMNode beanNode) {
		super(offset, length, missingEndQuote);
		this.beanNode = beanNode;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		IStructuredModel model = null;
		try {
			if (document instanceof IStructuredDocument) {
				model = StructuredModelManager.getModelManager().getModelForEdit((IStructuredDocument) document);
				model.beginRecording(this);
			}

			Document ownerDocument = beanNode.getOwnerDocument();

			AttrImpl attrNode = (AttrImpl) ownerDocument.createAttribute(BeansSchemaConstants.ATTR_FACTORY_METHOD);
			beanNode.getAttributes().setNamedItem(attrNode);

			FormatProcessorXML formatter = new FormatProcessorXML();
			formatter.formatNode(beanNode);

			if (model != null) {
				model.endRecording(this);
			}

			LinkedModeModel linkModel = new LinkedModeModel();
			LinkedPositionGroup group = new LinkedPositionGroup();
			attrNode = ((AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_FACTORY_METHOD));

			try {
				group.addPosition(new LinkedPosition(document, attrNode.getValueRegionStartOffset() + 1, 0));
				linkModel.addGroup(group);

			}
			catch (BadLocationException e) {
				StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Unable to create linked model for factory method quick fix"));
			}

			ITextViewer viewer = null;
			IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (editor != null && editor instanceof IConfigEditor) {
				viewer = ((IConfigEditor) editor).getTextViewer();
			}

			if (viewer != null) {
				try {
					linkModel.forceInstall();
					LinkedModeUI ui = new LinkedModeUI(linkModel, viewer);
					ui.enter();
				}
				catch (BadLocationException e) {
					StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Unable to create linked model for factory method quick fix"));
				}
			}
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
			}
		}
	}

	public String getDisplayString() {
		return "Add factory-method attribute";
	}

	public Image getImage() {
		return null;
	}

}
