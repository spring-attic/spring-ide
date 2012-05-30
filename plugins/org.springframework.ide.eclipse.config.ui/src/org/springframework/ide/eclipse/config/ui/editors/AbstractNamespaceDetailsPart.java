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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;


/**
 * This class is an extension to {@link AbstractConfigDetailsPart} that is
 * suited to displaying and editing the attributes of a single element in a
 * Spring configuration file. The element displayed must be a valid element in
 * one of the Spring namespaces.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public class AbstractNamespaceDetailsPart extends AbstractConfigDetailsPart {

	public static final String DOCS_SPRINGFRAMEWORK_25 = "http://static.springsource.org/spring/docs/2.5.x/reference/index.html"; //$NON-NLS-1$

	public static final String DOCS_SPRINGFRAMEWORK_30 = "http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/"; //$NON-NLS-1$

	private final SpringConfigContentAssistProcessor processor;

	protected AbstractConfigSectionPart docsSection;

	private final String docsUrl;

	/**
	 * Constructs a details part with a reference to its master part.
	 * 
	 * @param master the page's master part
	 */
	public AbstractNamespaceDetailsPart(AbstractConfigMasterPart master) {
		this(master, DOCS_SPRINGFRAMEWORK_30);
	}

	public AbstractNamespaceDetailsPart(AbstractConfigMasterPart master, String docsUrl) {
		super(master);
		this.docsUrl = docsUrl;
		processor = new SpringConfigContentAssistProcessor();
	}

	@Override
	public void createContents(Composite parent) {
		super.createContents(parent);
		createDocumentationSection(parent);
	}

	@Override
	protected void createDetailsContent(Composite client) {
		// No callers.
	}

	@Override
	protected void createDetailsSection(Composite parent) {
		if (requiresDetailsSection()) {
			super.createDetailsSection(parent);
		}
	}

	@Override
	public SpringConfigDetailsSectionPart createDetailsSectionPart(AbstractConfigEditor editor, IDOMElement input,
			Composite parent, FormToolkit toolkit) {
		return new SpringConfigDetailsSectionPart(editor, input, parent, toolkit);
	}

	/**
	 * Creates the documentation section of the form part, pointing to the
	 * Spring Core documentation. Clients may override if necessary.
	 * 
	 * @param parent the parent composite
	 */
	protected void createDocumentationSection(Composite parent) {
		if (requiresDocumentationSection()) {
			docsSection = createDocumentationSectionPart(getConfigEditor(), getInput(), parent, toolkit);
			docsSection.createContent();
			alignSectionHeaderWithMaster(docsSection.getSection());
		}
	}

	public SpringConfigDocumentationSectionPart createDocumentationSectionPart(AbstractConfigEditor editor,
			IDOMElement input, Composite parent, FormToolkit toolkit) {
		return new SpringConfigDocumentationSectionPart(editor, input, parent, toolkit, docsUrl);
	}

	@Override
	public void dispose() {
		processor.release();
		super.dispose();
	}

	@Override
	protected String getDetailsSectionDescription() {
		// No callers.
		return null;
	}

	@Override
	protected String getDetailsSectionTitle() {
		// No callers.
		return null;
	}

	/**
	 * Returns the documentation section of the form part.
	 * 
	 * @return documentation section
	 */
	public AbstractConfigSectionPart getDocumentationSection() {
		return docsSection;
	}

	@Override
	public void refresh() {
		if (docsSection != null) {
			docsSection.refresh();
		}
		super.refresh();
	}

	private boolean requiresDetailsSection() {
		if (processor.getAttributeDeclarations(getInput()).isEmpty() && !processor.allowsCharacterData(getInput())) {
			return false;
		}
		return true;
	}

	private boolean requiresDocumentationSection() {
		String documentation = processor.getDocumentation(getInput());
		return documentation != null && documentation.trim().length() > 0;
	}

	@Override
	protected void updateInput(ISelection selection) {
		super.updateInput(selection);
		if (docsSection != null) {
			docsSection.setFormInput(getInput());
		}
	}

}
