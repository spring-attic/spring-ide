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

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.validation.IXmlValidationContext;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.SpringProject;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationProblemMarker;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.core.type.asm.CachingClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.w3c.dom.Node;

/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class XmlValidationContextHelper extends AbstractValidationContext implements IXmlValidationContext {

	private final IDOMNode node;

	private final BeansEditorValidator validator;

	private final boolean reportError;

	private final AttrImpl attribute;

	private final IReporter reporter;

	private boolean errorFound;

	private ClassReaderFactory classReaderFactory;

	private final IProject project;

	private IProjectClassLoaderSupport projectClassLoaderSupport;

	private final IFile file;

	private final BeansValidationContext delegateContext;

	public XmlValidationContextHelper(BeansEditorValidator validator, AttrImpl attribute, IDOMNode node, IFile file,
			IBeansConfig config, IResourceModelElement contextElement, IReporter reporter, boolean reportError) {
		super(config, contextElement);
		this.validator = validator;
		this.reporter = reporter;
		this.reportError = reportError;
		this.attribute = attribute;
		this.node = node;

		this.project = file.getProject();
		this.file = file;
		this.delegateContext = new BeansValidationContext(config, new SpringProject(SpringCore.getModel(),
				file.getProject()));

		this.errorFound = false;
	}

	@Override
	public void error(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		int severity = getSeverity(message, IMessage.HIGH_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), node, message, reporter,
					QuickfixProcessorFactory.NAMESPACE_ELEMENTS, false, severity, problemId, attributes);
		}

		errorFound = true;

		super.error(element, problemId, message, attributes);
	}

	public void error(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
		int severity = getSeverity(message, IMessage.HIGH_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), this.node, message, reporter,
					QuickfixProcessorFactory.NAMESPACE_ELEMENTS, false, severity, problemId, attributes);
		}

		errorFound = true;

		super.error(new SpringProject(SpringCore.getModel(), project), problemId, message, attributes);
	}

	public ClassReaderFactory getClassReaderFactory() {
		if (this.classReaderFactory == null) {
			this.classReaderFactory = new CachingClassReaderFactory(JdtUtils.getClassLoader(project,
					BeansCorePlugin.getClassLoader()));
		}
		return this.classReaderFactory;
	}

	public BeanDefinitionRegistry getCompleteRegistry() {
		return delegateContext.getCompleteRegistry();
	}

	@Override
	public IResourceModelElement getContextElement() {
		return delegateContext.getContextElement();
	}

	public boolean getErrorFound() {
		return errorFound;
	}

	public BeanDefinitionRegistry getIncompleteRegistry() {
		return delegateContext.getIncompleteRegistry();
	}

	public IProjectClassLoaderSupport getProjectClassLoaderSupport() {
		if (this.projectClassLoaderSupport == null) {
			this.projectClassLoaderSupport = JdtUtils.getProjectClassLoaderSupport(project, null);
		}
		return this.projectClassLoaderSupport;
	}

	public Set<BeanDefinition> getRegisteredBeanDefinition(String beanName, String beanClass) {
		return delegateContext.getRegisteredBeanDefinition(beanName, beanClass);
	}

	public IProject getRootElementProject() {
		return project;
	}

	public IResource getRootElementResource() {
		return file;
	}

	public List<ToolAnnotationData> getToolAnnotation(Node n, String attributeName) {
		return delegateContext.getToolAnnotation(n, attributeName);
	}

	@Override
	public void info(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		int severity = getSeverity(message, IMessage.LOW_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), this.node, message, reporter,
					QuickfixProcessorFactory.NAMESPACE_ELEMENTS, false, severity, problemId, attributes);
		}

		errorFound = true;

		super.info(new SpringProject(SpringCore.getModel(), project), problemId, message, attributes);
	}

	public void info(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
		int severity = getSeverity(problemId, IMessage.LOW_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), this.node, message, reporter,
					QuickfixProcessorFactory.NAMESPACE_ELEMENTS, false, severity, problemId, attributes);
		}

		errorFound = true;

		super.info(new SpringProject(SpringCore.getModel(), project), problemId, message, attributes);
	}

	public boolean isBeanRegistered(String beanName, String beanClass) {
		Set<BeanDefinition> bds = getRegisteredBeanDefinition(beanName, beanClass);
		return bds != null && bds.size() > 0;
	}

	@Override
	public void warning(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		int severity = getSeverity(problemId, IMessage.NORMAL_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), this.node, message, reporter,
					QuickfixProcessorFactory.NAMESPACE_ELEMENTS, false, severity, problemId, attributes);
		}

		errorFound = true;

		super.warning(new SpringProject(SpringCore.getModel(), project), problemId, message, attributes);
	}

	public void warning(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
		int severity = getSeverity(problemId, IMessage.NORMAL_SEVERITY);
		if (reportError && severity >= 0) {
			validator.createAndAddMessage(attribute.getValueRegion(), this.node, message, reporter,
					QuickfixProcessorFactory.NAMESPACE_ELEMENTS, false, severity, problemId, attributes);
		}

		errorFound = true;

		super.warning(new SpringProject(SpringCore.getModel(), project), problemId, message, attributes);
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
}
