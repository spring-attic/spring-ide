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
package org.springframework.ide.eclipse.quickfix.validator.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.IncrementalReporter;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.tests.AbstractQuickfixTestCase;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractBeanValidationTestCase extends AbstractQuickfixTestCase {

	protected static final Set<IResourceModelElement> getContextElements(IBeansConfig config) {
		Set<IResourceModelElement> contextElements = new LinkedHashSet<IResourceModelElement>();
		contextElements.addAll(BeansModelUtils.getConfigSets(config));
		if (contextElements.isEmpty()) {
			contextElements.add(config);
		}
		return contextElements;

	}

	protected BeansEditorValidator validator;

	protected IncrementalReporter reporter;

	protected final String QUICK_ASSIST_PROCESSOR = "org.eclipse.jface.text.quickassist.IQuickAssistProcessor";

	// protected boolean checkMessageTexts(List<IMessage> messages, String...
	// expectedTexts) {
	// Set<String> result = new HashSet<String>();
	// for (IMessage message : messages) {
	// String text = message.getText();
	// if (text != null && text.length() > 0) {
	// result.add(text);
	// }
	// }
	//
	// Set<String> expectedResult = new HashSet<String>();
	// for (String expectedText : expectedTexts) {
	// expectedResult.add(expectedText);
	// }
	//
	// return result.equals(expectedResult);
	// }

	protected void createBeansEditorValidator(String name) throws Exception {
		copyProjectCreateDocument(name);

		validator = new BeansEditorValidator();
		validator.connect(document);
		reporter = createReporter();
	}

	private IncrementalReporter createReporter() {
		return new IncrementalReporter(new NullProgressMonitor());
	}

	protected IMessage getErrorMessage(List<IMessage> messages) {
		for (IMessage message : messages) {
			if (message.getSeverity() == IMessage.HIGH_SEVERITY) {
				return message;
			}
		}
		return null;
	}

	protected Object getProcessor(List<IMessage> messages, Class<?> clazz) {
		for (IMessage message : messages) {
			Object processor = message.getAttribute(QUICK_ASSIST_PROCESSOR);
			if (processor != null && processor.getClass().equals(clazz)) {
				return processor;
			}
		}
		return null;
	}

	protected List<String> getVisibleMessages(List<IMessage> messages) {
		List<String> result = new ArrayList<String>();
		for (IMessage message : messages) {
			String text = message.getText();
			if (text != null && text.length() > 0 && !result.contains(text)) {
				result.add(text);
			}
		}

		Collections.sort(result);
		return result;
	}

	protected IMessage getWarningMessage(List<IMessage> messages) {
		for (IMessage message : messages) {
			if (message.getSeverity() == IMessage.NORMAL_SEVERITY) {
				return message;
			}
		}
		return null;
	}

	@Override
	protected void tearDown() throws Exception {
		reporter.removeAllMessages(validator);
		super.tearDown();
	}
}
