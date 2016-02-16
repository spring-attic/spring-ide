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
package org.springframework.ide.eclipse.quickfix.validator.helper;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class BeansValidationContextHelper extends BeansValidationContext {

	private final IProject project;

	private final AttrImpl attribute;

	private final IReporter reporter;

	private final BeansEditorValidator validator;

	private final IDOMNode node;

	private final QuickfixProcessorFactory quickfixFactory;

	private final boolean affectsWholeBean;

	private final boolean reportError;

	private boolean errorFound;

	private final Set<String> problemIdToIgnore;

	public BeansValidationContextHelper(AttrImpl attribute, IDOMNode node, IResourceModelElement rootElement,
			IProject project, IReporter reporter, BeansEditorValidator validator,
			QuickfixProcessorFactory quickfixFactory, boolean affectsWholeBean, boolean reportError, IBeansConfig config) {
		this(attribute, node, rootElement, project, reporter, validator, quickfixFactory, affectsWholeBean,
				reportError, config, null);
	}

	public BeansValidationContextHelper(AttrImpl attribute, IDOMNode node, IResourceModelElement rootElement,
			IProject project, IReporter reporter, BeansEditorValidator validator,
			QuickfixProcessorFactory quickfixFactory, boolean affectsWholeBean, boolean reportError,
			IBeansConfig config, Set<String> problemIdToIgnore) {
		super(config, rootElement);
		this.attribute = attribute;
		this.node = node;
		this.project = project;
		this.reporter = reporter;
		this.validator = validator;
		this.quickfixFactory = quickfixFactory;
		this.affectsWholeBean = affectsWholeBean;
		this.reportError = reportError;
		this.problemIdToIgnore = problemIdToIgnore;

		new HashMap<String, Set<BeanDefinition>>();
		this.errorFound = false;
	}

	@Override
	public void error(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		if (problemIdToIgnore != null && problemIdToIgnore.contains(problemId)) {
			return;
		}

		int severity = getSeverity(problemId, IMessage.HIGH_SEVERITY);
		if (reportError && severity >= 0) {
			if (affectsWholeBean) {
				validator.createAndAddMessageForNode(node, node, attribute.getValue(), message, reporter,
						quickfixFactory, severity, problemId, attributes);
			}
			else {
				validator.createAndAddMessage(attribute.getValueRegion(), node, message, reporter, quickfixFactory,
						affectsWholeBean, severity, problemId, attributes);
			}
		}
		errorFound = true;

		super.error(element, problemId, message, attributes);
	}

	public boolean getErrorFound() {
		return errorFound;
	}

	@Override
	public IProject getRootElementProject() {
		return project;
	}

	@Override
	protected int getSeverity(String messageId, int defaultSeverity) {
		switch (defaultSeverity) {
		case IMessage.HIGH_SEVERITY:
			defaultSeverity = IValidationProblemMarker.SEVERITY_ERROR;
			break;
		case IMessage.NORMAL_SEVERITY:
			defaultSeverity = IValidationProblemMarker.SEVERITY_WARNING;
			break;
		case IMessage.LOW_SEVERITY:
			defaultSeverity = IValidationProblemMarker.SEVERITY_INFO;
			break;
		}

		int severity = super.getSeverity(messageId, defaultSeverity);

		switch (severity) {
		case IValidationProblemMarker.SEVERITY_ERROR:
			return IMessage.HIGH_SEVERITY;
		case IValidationProblemMarker.SEVERITY_WARNING:
			return IMessage.NORMAL_SEVERITY;
		case IValidationProblemMarker.SEVERITY_INFO:
			return IMessage.LOW_SEVERITY;
		default:
			return -1;
		}
	}

	@Override
	public void info(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		if (problemIdToIgnore != null && problemIdToIgnore.contains(problemId)) {
			return;
		}

		int severity = getSeverity(problemId, IMessage.LOW_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), node, message, reporter, quickfixFactory,
					affectsWholeBean, severity, problemId, attributes);
		}

		super.info(element, problemId, message, attributes);
	}

	@Override
	public void warning(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		if (problemIdToIgnore != null && problemIdToIgnore.contains(problemId)) {
			return;
		}

		int severity = getSeverity(problemId, IMessage.NORMAL_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), node, message, reporter, quickfixFactory,
					affectsWholeBean, severity, problemId, attributes);
		}

		errorFound = true;

		super.warning(element, problemId, message, attributes);
	}

}
