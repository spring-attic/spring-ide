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
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;


/**
 * @author Leo Dos Santos
 * @author Steffen Pingel
 */
@SuppressWarnings("restriction")
public class SpringConfigDocumentationSectionPart extends AbstractConfigSectionPart {

	private final FormToolkit toolkit;

	private final String docsUrl;

	private final SpringConfigContentAssistProcessor processor;

	public SpringConfigDocumentationSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit) {
		this(editor, input, parent, toolkit, null);
	}

	public SpringConfigDocumentationSectionPart(AbstractConfigEditor editor, IDOMElement input, Composite parent,
			FormToolkit toolkit, String docsUrl) {
		super(editor, input, parent, toolkit, Section.TITLE_BAR);
		this.toolkit = toolkit;
		this.docsUrl = docsUrl;
		processor = editor.getXmlProcessor();
	}

	@Override
	public void createContent() {
		Section helpSection = getSection();
		helpSection.setLayout(new GridLayout());
		helpSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		helpSection.setText(Messages.getString("AbstractNamespaceDetailsPart.DOCUMENTATION_SECTION_TITLE")); //$NON-NLS-1$

		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 0;
		layout.rightMargin = 0;

		Composite helpClient = toolkit.createComposite(helpSection);
		helpClient.setLayout(layout);
		helpClient.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		helpSection.setClient(helpClient);

		Text text = new Text(helpClient, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		// GC gc = new GC(text);
		// FontMetrics fm = gc.getFontMetrics();
		// int height = 5 * fm.getHeight();
		// gc.dispose();

		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
		// data.maxHeight = text.computeSize(SWT.DEFAULT, height).y;
		text.setLayoutData(data);

		String documentation = processor.getDocumentation(getInput());
		text.setText(documentation);

		if (docsUrl != null) {
			Hyperlink link = toolkit.createHyperlink(helpClient,
					Messages.getString("AbstractNamespaceDetailsPart.EXTERNAL_DOCUMENTATION_LINK"), SWT.WRAP); //$NON-NLS-1$
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					TasksUiUtil.openUrl(docsUrl);
				}
			});
		}
	}

}
