package org.springframework.ide.eclipse.beans.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;

public class BeansContentAssistProcessor extends XMLContentAssistProcessor
										   implements IPropertyChangeListener {
	private IEditorPart editor;
	private IType[] cachedClasses;
	
	public BeansContentAssistProcessor(IEditorPart editor) {
		this.editor = editor;
	}

	protected void addAttributeValueProposals(
								   ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();

		// Find the attribute region and name for which this position should
		//have a value proposed
		IStructuredDocumentRegion open =
									   node.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(request.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() ==
									 DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		String matchString = request.getMatchString();
		if (matchString == null) {
			matchString = "";
		}
		if (matchString.length() > 0 && (matchString.startsWith("\"") ||
												matchString.startsWith("'"))) {
			matchString = matchString.substring(1);
		}

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null) {
			String attributeName = open.getText(nameRegion);
			String proposedInfo = "info";
			
			if ("action".equals(node.getNodeName())) {
				if ("pluginId".equals(attributeName)) {

					// get all registered plugin ids
					String[] ns = Platform.getExtensionRegistry().
															   getNamespaces();
					Image image = XMLEditorPluginImageHelper.getInstance().
							 getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE);
					for (int j = 0; j < ns.length; j++) {
						String pluginId = ns[j];
						if (pluginId.startsWith(matchString)) {
							CustomCompletionProposal proposal =
								 new CustomCompletionProposal("\"" + pluginId +
								 "\"", request.getReplacementBeginPosition(),
								 request.getReplacementLength(),
								 pluginId.length() + 1, image, "\""+ pluginId +
								 "\"", null, proposedInfo,
								 XMLRelevanceConstants.R_XML_ATTRIBUTE_VALUE);
							request.addProposal(proposal);
						}
					}
				} else if ("class".equals(attributeName)) {

					// get all classes
					if (editor.getEditorInput() instanceof IFileEditorInput) {
						IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
						IJavaProject project = JavaCore.create(file.getProject());

						try {
							IType cheatsheetInterface = project.findType(
									"org.eclipse.jface.action.IAction");
							if (cheatsheetInterface != null) {
								ITypeHierarchy hier = cheatsheetInterface.newTypeHierarchy(project, new NullProgressMonitor());
								IType[] classes = hier.getAllSubtypes(cheatsheetInterface);
							
								if (classes.length == 0) {
									// nothing has changed, use cached instance instead
									classes = cachedClasses;
								} else {
									cachedClasses = classes;
								}
								for (int j = 0; j < classes.length; j++) {
									IType type = classes[j];
									if (!Flags.isAbstract(type.getFlags())) {
										String name = type.getFullyQualifiedName();
										if (name.startsWith(matchString)) {
											CustomCompletionProposal proposal = new CustomCompletionProposal("\"" + name + "\"", //$NON-NLS-2$//$NON-NLS-1$
													request.getReplacementBeginPosition(), request.getReplacementLength(), name.length() + 1, XMLEditorPluginImageHelper.getInstance().getImage(XMLEditorPluginImages.IMG_OBJ_ATTRIBUTE),
													"\"" + name + "\"", null, proposedInfo, XMLRelevanceConstants.R_XML_ATTRIBUTE_VALUE);
											request.addProposal(proposal);
										}
									}
								}
							}
						} catch (CoreException e) {
							BeansEditorPlugin.log(e);
						}
					}
				}
			}
			if (request == null) {
				super.addAttributeValueProposals(request);
			}
		}
	}
}
