package org.springframework.ide.eclipse.beans.ui.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
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

	public BeansContentAssistProcessor(IEditorPart editor) {
		this.editor = editor;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	protected void addAttributeValueProposals(
								   ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();

		// Find the attribute region and name for which this position should
		// have a value proposed
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
			if ("bean".equals(node.getNodeName())) {
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
								 "\"", null, null,
								 XMLRelevanceConstants.R_XML_ATTRIBUTE_VALUE);
							request.addProposal(proposal);
						}
					}
				} else if ("class".equals(attributeName)) {
					addClassAttributeValueProposals(request, matchString);
				}
			}
			if (request == null) {
				super.addAttributeValueProposals(request);
			}
		}
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request,
												 String prefix) {
		if (prefix.length() > 0) {
			Map types = getJavaTypes(prefix);
			Iterator names = types.keySet().iterator();
			while (names.hasNext()) {
				String name = (String) names.next();
				CustomCompletionProposal proposal =
							 new CustomCompletionProposal(name,
							 request.getReplacementBeginPosition() + 1,
							 request.getReplacementLength() - 2, name.length(),
							 (Image) types.get(name), name, null, null,
							 XMLRelevanceConstants.R_XML_ATTRIBUTE_VALUE);
				request.addProposal(proposal);
			}
		} else {
			setErrorMessage("Prefix too short");
			
		}
	}

	private Map getJavaTypes(String prefix) {
		Map types = new HashMap();
		if (editor.getEditorInput() instanceof IFileEditorInput) {
			try {
				IFile file = ((IFileEditorInput)
											editor.getEditorInput()).getFile();
				IJavaProject project = JavaCore.create(file.getProject());
						IJavaElement[] packages = project.getPackageFragments();
						for (int j = 0; j < packages.length; j++) {
							IPackageFragment pkg = (IPackageFragment) packages[j];
							if (pkg.exists()) {
								String name = pkg.getElementName();
								if (name.startsWith(prefix)) {
//									if (name.length() > 0  && !types.containsKey(name)) {
//									types.put(name, JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE));
//									}
								IJavaElement[] children = pkg.getChildren();
								for (int k = 0; k < children.length; k++) {
									IJavaElement element = children[k];
									if (element.exists()) {
										String n = "";
										Image image = null;
										if (element instanceof ICompilationUnit) {
 											n = ((ICompilationUnit) element).getTypes()[0].getFullyQualifiedName();
 											image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CUNIT);
										} else if (element instanceof IClassFile) {
 											n = ((IClassFile) element).getType().getFullyQualifiedName();
 											image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
										}
								if (n.startsWith(prefix) &&
													!types.containsKey(n)) {
									types.put(n, image);
								}
									}
								}
								}
							}
				}
			} catch (JavaModelException e) {
				// nothing to do
			}
		}
		return types;
	}
}
