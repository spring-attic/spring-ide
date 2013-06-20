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
package org.springframework.ide.eclipse.quickfix;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.AnnotationInfo;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.ISourceValidator;
import org.eclipse.wst.sse.ui.internal.reconcile.validator.IncrementalReporter;
import org.eclipse.wst.validation.internal.core.Message;
import org.eclipse.wst.validation.internal.core.ValidationException;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.validation.internal.provisional.core.IValidationContext;
import org.eclipse.wst.validation.internal.provisional.core.IValidator;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.processors.BeanQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.BeanValidatorVisitor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Source validator for beans XML editor.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansEditorValidator implements ISourceValidator, IValidator {

	private static class ContextElementVisitor implements IModelElementVisitor {

		private final Set<IResourceModelElement> contextElements;

		private final IResource resource;

		private IBeansConfig currentConfig = null;

		public ContextElementVisitor(IResource resource, Set<IResourceModelElement> contextElements) {
			this.resource = resource;
			this.contextElements = contextElements;
		}

		public boolean visit(IModelElement element, IProgressMonitor monitor) {
			if (element instanceof IBeansModel) {
				return true;
			}
			else if (element instanceof IBeansProject) {
				return true;
			}
			else if (element instanceof IImportedBeansConfig) {
				if (resource.equals(((IImportedBeansConfig) element).getElementResource())) {
					contextElements.add(currentConfig);
				}
				return true;
			}
			else if (element instanceof IBeansConfig) {
				this.currentConfig = (IBeansConfig) element;
				return true;
			}
			else if (element instanceof IBeansImport) {
				for (IImportedBeansConfig config : ((IBeansImport) element).getImportedBeansConfigs()) {
					config.accept(this, monitor);
				}
				return false;
			}
			else if (element instanceof IBeansConfigSet) {
				for (IBeansConfig config : ((IBeansConfigSet) element).getConfigs()) {
					if (resource.equals(config.getElementResource())) {
						contextElements.add((IBeansConfigSet) element);
						break;
					}
				}
				return false;
			}
			return false;
		}
	}

	protected class LocalizedMessage extends Message {

		private String _message = null;

		public LocalizedMessage(int severity, String messageText) {
			this(severity, messageText, null);
		}

		public LocalizedMessage(int severity, String messageText, IResource targetObject) {
			this(severity, messageText, (Object) targetObject);
		}

		public LocalizedMessage(int severity, String messageText, Object targetObject) {
			super(null, severity, null);
			setLocalizedMessage(messageText);
			setTargetObject(targetObject);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LocalizedMessage) {
				LocalizedMessage m = (LocalizedMessage) obj;
				return m.getText().equals(getText()) && m.getOffset() == getOffset();
			}

			return false;
		}

		public String getLocalizedMessage() {
			return _message;
		}

		@Override
		public String getText() {
			return getLocalizedMessage();
		}

		@Override
		public String getText(ClassLoader cl) {
			return getLocalizedMessage();
		}

		@Override
		public String getText(Locale l) {
			return getLocalizedMessage();
		}

		@Override
		public String getText(Locale l, ClassLoader cl) {
			return getLocalizedMessage();
		}

		@Override
		public int hashCode() {
			return (getText() + getOffset()).hashCode();
		}

		public void setLocalizedMessage(String message) {
			_message = message;
		}
	}

	private IDocument document = null;

	private IFile file = null;

	private IStructuredModel model = null;

	private IProject project;

	// add node and all children node to checked nodes
	private void addCheckedNodes(IDOMNode node, Set<IDOMNode> checkedNodes) {
		checkedNodes.add(node);
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if (child instanceof IDOMNode) {
				addCheckedNodes((IDOMNode) child, checkedNodes);
			}
		}

	}

	public void cleanup(IReporter reporter) {
		reporter.removeAllMessages(this);
	}

	public void connect(IDocument document) {
		this.document = document;
		if (model == null) {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
		}
		if (model != null) {
			String location = model.getBaseLocation();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(location);
			if (root.getFullPath().append(filePath).segmentCount() < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
				disconnect(document);
			}
			else {
				file = root.getFile(filePath);
				project = file.getProject();
			}
		}
	}

	public void createAndAddEmptyMessage(ITextRegion valueRegion, IDOMNode parentNode, String messageText,
			IReporter reporter, QuickfixProcessorFactory quickfixFactory, String problemId,
			ValidationProblemAttribute... problemAttributes) {
		createAndAddMessage(valueRegion, parentNode, messageText, reporter, quickfixFactory, false, false,
				IMessage.ALL_MESSAGES, problemId, problemAttributes);
	}

	private void createAndAddMessage(int offset, int length, int emptyMsgOffset, int emptyMsgLength,
			boolean missingEndQuote, IDOMNode beanNode, String text, String messageText, IReporter reporter,
			QuickfixProcessorFactory quickfixFactory, boolean showErrorMessage, int severity, String problemId,
			ValidationProblemAttribute... problemAttributes) {
		if (beanNode == null || text == null || messageText == null || document == null) {
			return;
		}

		if (showErrorMessage) {
			IMessage message = new LocalizedMessage(severity, messageText);
			message.setOffset(offset);
			message.setLength(length);
			try {
				message.setLineNo(document.getLineOfOffset(offset) + 1);
			}
			catch (BadLocationException e) {
				message.setLineNo(-1);
			}
			reporter.addMessage(this, message);
		}

		// TODO: investigate better way to make suggestion works for the last
		// position
		// messageEmpty does not show up in the editor, it is only
		// used for showing quick assist in the last cursor position
		IMessage messageEmpty = new LocalizedMessage(IMessage.ALL_MESSAGES, messageText);

		messageEmpty.setOffset(emptyMsgOffset);
		messageEmpty.setLength(emptyMsgLength);
		try {
			messageEmpty.setLineNo(document.getLineOfOffset(offset) + 1);
		}
		catch (BadLocationException e) {
			messageEmpty.setLineNo(-1);
		}

		if (reporter instanceof IncrementalReporter) {
			BeanQuickAssistProcessor processor = quickfixFactory.create(offset, length, text, missingEndQuote,
					beanNode, this, problemId, problemAttributes);

			if (processor != null) {
				messageEmpty.setAttribute(IQuickAssistProcessor.class.getName(), processor);

				AnnotationInfo info = new QuickfixAnnotationInfo(messageEmpty);

				IncrementalReporter incrementalReporter = (IncrementalReporter) reporter;
				AnnotationInfo[] existingInfos = incrementalReporter.getAnnotationInfo();
				for (AnnotationInfo existingInfo : existingInfos) {
					IMessage existingMessage = existingInfo.getMessage();

					if (existingMessage.getOffset() != messageEmpty.getOffset()) {
						continue;
					}
					if (!existingMessage.getText().equals(messageEmpty.getText())) {
						continue;
					}

					Object existingProcessor = existingMessage.getAttribute(IQuickAssistProcessor.class.getName());
					if (existingProcessor != null && existingProcessor.equals(processor)) {
						return;
					}
				}

				incrementalReporter.addAnnotationInfo(this, info);
			}
		}
	}

	public void createAndAddMessage(ITextRegion valueRegion, IDOMNode parentNode, String messageText,
			IReporter reporter, QuickfixProcessorFactory quickfixFactory, boolean affectsWholeBean, boolean showError,
			int severity, String problemId, ValidationProblemAttribute... problemAttributes) {
		// IMessage message = new LocalizedMessage(IMessage.HIGH_SEVERITY,
		// messageText);

		// TODO: investigate better way to make suggestion works for the last
		// position
		// messageEmpty does not show up in the editor, it is only
		// used for showing quick assist in the last cursor position
		// IMessage messageEmpty = new LocalizedMessage(IMessage.ALL_MESSAGES,
		// messageText);
		int offset = valueRegion.getStart() + parentNode.getStartOffset();
		boolean missingEndQuote = false;

		if (document == null) {
			return;
		}

		String text = document.get().substring(offset, offset + valueRegion.getLength());
		text = text.trim();
		int length = text.length();
		if (text.startsWith("\'") || text.startsWith("\"")) {
			offset++;
			length--;
			text = text.substring(1);
		}

		if (text.endsWith("\'") || text.endsWith("\"")) {
			length--;
			text = text.substring(0, text.length() - 1);
		}
		else {
			missingEndQuote = true;
		}

		int msgOffset, msgLength;

		if (affectsWholeBean) {
			msgOffset = parentNode.getStartOffset();
			msgLength = parentNode.getEndOffset() - msgOffset;
			offset = msgOffset;
			length = msgLength;
		}
		else {
			msgOffset = offset;
			msgLength = length;
		}

		createAndAddMessage(offset, length, msgOffset, msgLength + 1, missingEndQuote, parentNode, text, messageText,
				reporter, quickfixFactory, showError, severity, problemId, problemAttributes);
	}

	public void createAndAddMessage(ITextRegion valueRegion, IDOMNode parentNode, String messageText,
			IReporter reporter, QuickfixProcessorFactory quickfixFactory, boolean affectsWholeBean, int severity,
			String problemId, ValidationProblemAttribute... problemAttributes) {
		createAndAddMessage(valueRegion, parentNode, messageText, reporter, quickfixFactory, affectsWholeBean, true,
				severity, problemId, problemAttributes);
	}

	public void createAndAddMessage(ITextRegion valueRegion, IDOMNode parentNode, String messageText,
			IReporter reporter, QuickfixProcessorFactory quickfixFactory, int severity, String problemId,
			ValidationProblemAttribute... problemAttributes) {
		createAndAddMessage(valueRegion, parentNode, messageText, reporter, quickfixFactory, false, true, severity,
				problemId, problemAttributes);
	}

	public void createAndAddMessageForNode(IDOMNode node, IDOMNode beanNode, String text, String messageText,
			IReporter reporter, QuickfixProcessorFactory quickfixFactory, int severity, String problemId,
			ValidationProblemAttribute... problemAttributes) {
		int offset = node.getStartOffset();
		int length = node.getEndOffset() - offset;
		boolean missingEndQuote = false;

		createAndAddMessage(offset, length, offset, length + 1, missingEndQuote, beanNode, text, messageText, reporter,
				quickfixFactory, true, severity, problemId, problemAttributes);
	}

	public void disconnect(IDocument document) {
		if (this.model != null) {
			model.releaseFromRead();
			model = null;
		}
		this.document = null;
	}

	private final Set<IResourceModelElement> getContextElements(IBeansConfig config) {
		Set<IResourceModelElement> contextElements = new LinkedHashSet<IResourceModelElement>();

		BeansCorePlugin.getModel().accept(new ContextElementVisitor(config.getElementResource(), contextElements),
				new NullProgressMonitor());

		if (contextElements.isEmpty()) {
			contextElements.add(config);
		}
		return contextElements;

	}

	public IFile getFile() {
		return file;
	}

	private IDOMNode getNodeAt(int documentOffset, int length) {
		IndexedRegion node = null;
		if (model != null) {
			// int lastOffset = documentOffset;
			node = model.getIndexedRegion(documentOffset);
			if (node == null) {
				return null;
			}
			if (node.getStartOffset() < documentOffset || node.getStartOffset() >= documentOffset + length) {
				node = null;
			}
			// while ((node == null) && lastOffset >= 0) {
			// lastOffset--;
			// node = model.getIndexedRegion(lastOffset);
			// }
		}

		if (node instanceof IDOMNode && !(node instanceof TextImpl)) {
			return getNonTextNode((IDOMNode) node);
		}

		return null;
	}

	private IDOMNode getNonTextNode(IDOMNode node) {
		if (node == null || !(node instanceof IDOMText)) {
			return node;
		}
		return getNonTextNode((IDOMNode) node.getParentNode());
	}

	public IProject getProject() {
		return project;
	}

	public void validate(IRegion dirtyRegion, IValidationContext context, IReporter reporter) {
		if (document == null || !BeansCoreUtils.isBeansConfig(file)) {
			return;
		}

		if (!(document instanceof IStructuredDocument)) {
			return;
		}

		IStructuredDocumentRegion[] regions = ((IStructuredDocument) document).getStructuredDocumentRegions(
				dirtyRegion.getOffset(), dirtyRegion.getLength());

		Set<IDOMNode> checkedNodes = new HashSet<IDOMNode>();

		// long start = System.currentTimeMillis();

		for (IStructuredDocumentRegion region : regions) {
			IDOMNode node = getNodeAt(region.getStartOffset(), region.getLength());
			if (node != null && !checkedNodes.contains(node)) {
				validateNode(node, reporter);
				addCheckedNodes(node, checkedNodes);
			}
		}
		// System.out.println(String.format("%s, reconiling region %s:%s on %s",
		// (System.currentTimeMillis() - start),
		// dirtyRegion.getOffset(), dirtyRegion.getLength(),
		// file.getFullPath().toString()));
	}

	public void validate(IValidationContext helper, IReporter reporter) throws ValidationException {
	}

	private void validateNode(IDOMNode node, IReporter reporter) {
		IBeansModel model = BeansCorePlugin.getModel();
		Set<IBeansConfig> configs = model.getConfigs(BeansConfigId.create(file), true);
		for (IBeansConfig config : configs) {
			Set<IResourceModelElement> contextElements = getContextElements(config);
			for (IResourceModelElement contextElement : contextElements) {
				BeanValidatorVisitor visitor = new BeanValidatorVisitor(config, contextElement, reporter, this);
				if (visitor.visitNode(node, true, true)) {
					return;
				}
			}
		}

	}

}
