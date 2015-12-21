package org.springframework.ide.eclipse.data.jdt.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.data.internal.DataCorePlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * @since 3.2
 */
@SuppressWarnings("restriction")
public class FindByMethodCompletionProposal implements IJavaCompletionProposal, ICompletionProposalExtension2 {

	private final String propertyName;

	private final Class<?> propertyClass;

	private final Class<?> domainClass;

	private final int startOffset;

	private final int endOffset;

	private IRegion selectedRegion;

	private final ICompilationUnit cu;

	public FindByMethodCompletionProposal(String propertyName, Class<?> propertyClass, Class<?> domainClass,
			int startOffset, int endOffset, JavaContentAssistInvocationContext javaContext) {
		this.propertyName = propertyName;
		this.propertyClass = propertyClass;
		this.domainClass = domainClass;
		this.startOffset = startOffset;
		this.endOffset = endOffset;

		this.selectedRegion = new Region(startOffset, endOffset);
		cu = javaContext.getCompilationUnit();
	}

	public static String getMethodName(String propertyName) {
		return "findBy" + StringUtils.capitalize(propertyName);
	}

	private String getMethodString() {
		StringBuilder str = new StringBuilder();

		str.append(getMethodName(propertyName));
		str.append("(");
		str.append(propertyClass.getSimpleName());
		str.append(" ");
		str.append(propertyName.toLowerCase());
		str.append(") : ");
		str.append("List<");
		str.append(domainClass.getSimpleName());
		str.append(">");

		return str.toString();
	}

	public Point getSelection(IDocument document) {
		return new Point(selectedRegion.getOffset(), selectedRegion.getLength());
	}

	public String getAdditionalProposalInfo() {
		return null;
	}

	public String getDisplayString() {
		return getMethodString();
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
	}

	public IContextInformation getContextInformation() {
		return null;
	}

	public int getRelevance() {
		return 0;
	}

	private void beginCompoundChange(ITextViewer viewer) {
		if (viewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension = (ITextViewerExtension) viewer;
			IRewriteTarget rewriteTarget = extension.getRewriteTarget();
			rewriteTarget.beginCompoundChange();
		}
	}

	private void endCompoundChange(ITextViewer viewer) {
		if (viewer instanceof ITextViewerExtension) {
			ITextViewerExtension extension = (ITextViewerExtension) viewer;
			IRewriteTarget rewriteTarget = extension.getRewriteTarget();
			rewriteTarget.beginCompoundChange();
		}
	}

	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		IDocument document = viewer.getDocument();

		try {
			beginCompoundChange(viewer);

			int oldImportPos = getLastImportEndPosition();

			if (cu.getImport(propertyClass.getCanonicalName()) != null) {
				cu.createImport(propertyClass.getCanonicalName(), null, null);
			}

			if (cu.getImport(domainClass.getCanonicalName()) != null) {
				cu.createImport(domainClass.getCanonicalName(), null, null);
			}

			if (cu.getImport("java.util.List") != null) {
				cu.createImport("java.util.List", null, null);
			}

			int importOffset = getLastImportEndPosition() - oldImportPos;

			LinkedModeModel model = new LinkedModeModel();
			StringBuilder methodStr = new StringBuilder();

			int startPos, length;
			List<LinkedPositionGroup> groups = new ArrayList<LinkedPositionGroup>();
			LinkedPositionGroup group;

			group = new LinkedPositionGroup();
			startPos = startOffset + importOffset + methodStr.length();
			methodStr.append("List<");
			methodStr.append(domainClass.getSimpleName());
			methodStr.append(">");
			length = methodStr.length();
			group.addPosition(new LinkedPosition(document, startPos, length));
			groups.add(group);

			methodStr.append(" ");

			group = new LinkedPositionGroup();
			startPos = startOffset + importOffset + methodStr.length();
			String methodName = getMethodName(propertyName);
			methodStr.append(methodName);
			length = methodName.length();
			group.addPosition(new LinkedPosition(document, startPos, length));
			groups.add(group);

			methodStr.append("(");

			group = new LinkedPositionGroup();
			startPos = startOffset + importOffset + methodStr.length();
			String paramTypeName = propertyClass.getSimpleName();
			length = paramTypeName.length();
			methodStr.append(paramTypeName);
			group.addPosition(new LinkedPosition(document, startPos, length));
			groups.add(group);

			methodStr.append(" ");

			group = new LinkedPositionGroup();
			startPos = startOffset + importOffset + methodStr.length();
			String paramName = propertyName.toLowerCase();
			length = paramName.length();
			methodStr.append(paramName);
			group.addPosition(new LinkedPosition(document, startPos, length));
			groups.add(group);

			methodStr.append(");");

			document.replace(startOffset + importOffset, endOffset - startOffset, methodStr.toString());

			for (LinkedPositionGroup currGroup : groups) {
				model.addGroup(currGroup);
			}
			model.forceInstall();
			LinkedModeUI ui = new LinkedModeUI(model, viewer);
			ui.setExitPosition(viewer, startOffset + importOffset + methodStr.length(), 0, Integer.MAX_VALUE);
			ui.enter();

			selectedRegion = ui.getSelectedRegion();
		}
		catch (BadLocationException e) {
			StatusHandler.log(new Status(Status.ERROR, DataCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(Status.ERROR, DataCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
		finally {
			endCompoundChange(viewer);
		}
	}

	private int getLastImportEndPosition() {
		try {
			IImportDeclaration[] imports = cu.getImports();
			int lastPos = -1;
			for (IImportDeclaration currImport : imports) {
				ISourceRange sourceRange = currImport.getSourceRange();
				if (sourceRange != null) {
					int currPos = sourceRange.getOffset() + sourceRange.getLength();
					if (currPos > lastPos) {
						lastPos = currPos;
					}
				}
			}

			return lastPos;
		}
		catch (JavaModelException e) {
			return -1;
		}
	}

	public void selected(ITextViewer viewer, boolean smartToggle) {
	}

	public void unselected(ITextViewer viewer) {
	}

	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This method is no longer called by the framework and clients
	 * should overwrite {@link #apply(ITextViewer, char, int, int)} instead
	 */
	@Deprecated
	public void apply(IDocument document) {
		// not called anymore
	}
}
