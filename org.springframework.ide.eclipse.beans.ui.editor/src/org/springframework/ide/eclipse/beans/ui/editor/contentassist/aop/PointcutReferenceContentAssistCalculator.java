package org.springframework.ide.eclipse.beans.ui.editor.contentassist.aop;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistCalculator} implementation that calculates content
 * assist proposals for <code>pointcut-ref</code> attributes.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class PointcutReferenceContentAssistCalculator implements
		IContentAssistCalculator {

	private static final int RELEVANCE = 20;

	public void computeProposals(ContentAssistRequest request,
			String matchString, String attributeName, String namespace,
			String namepacePrefix) {
		addPointcutReferenceProposals(request, matchString);
	}

	private void addPointcutReferenceProposals(ContentAssistRequest request,
			String prefix) {
		if (prefix == null) {
			prefix = "";
		}
		IFile file = BeansEditorUtils.getFile(request);
		if (request.getNode().getOwnerDocument() != null) {
			searchPointcutElements(request, prefix, request.getParent(), file);
			searchPointcutElements(request, prefix, request.getParent()
					.getParentNode(), file);
		}
	}

	private void searchPointcutElements(ContentAssistRequest request,
			String prefix, Node node, IFile file) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("pointcut".equals(child.getLocalName())) {
				NamedNodeMap attributes = child.getAttributes();
				Node idAttribute = attributes.getNamedItem("id");
				if (idAttribute != null && idAttribute.getNodeValue() != null
						&& idAttribute.getNodeValue().startsWith(prefix)) {
					acceptSearchMatch(request, child, file);
				}
			}
		}
	}

	public void acceptSearchMatch(ContentAssistRequest request,
			Node pointcutNode, IFile file) {

		NamedNodeMap attributes = pointcutNode.getAttributes();
		Node idAttribute = attributes.getNamedItem("id");
		Node parentNode = pointcutNode.getParentNode();

		String pointcutName = idAttribute.getNodeValue();
		String replaceText = pointcutName;
		String fileName = file.getProjectRelativePath().toString();

		StringBuilder buf = new StringBuilder();
		buf.append(pointcutName);

		if (parentNode != null) {
			buf.append(" [");
			buf.append(parentNode.getNodeName());
			buf.append("]");
		}
		if (fileName != null) {
			buf.append(" - ");
			buf.append(fileName);
		}

		String displayText = buf.toString();
		Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_POINTCUT);

		BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
				replaceText, request.getReplacementBeginPosition(), request
						.getReplacementLength(), replaceText.length(), image,
				displayText, null, RELEVANCE, pointcutNode);

		request.addProposal(proposal);
	}
}
