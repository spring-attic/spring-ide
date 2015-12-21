package org.springframework.ide.eclipse.data.jdt.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.data.internal.DataCorePlugin;
import org.springframework.ide.eclipse.data.jdt.core.RepositoryInformation;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * 
 * Completion proposal computer for adding template methods for findBy...
 * methods.
 * 
 * @since 3.2
 * 
 */
@SuppressWarnings("restriction")
public class FindByMethodCompletionProposalComputer extends JavaCompletionProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		if (!(context instanceof JavaContentAssistInvocationContext)) {
			return super.computeCompletionProposals(context, monitor);
		}

		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

		CompletionContext coreContext = javaContext.getCoreContext();
		if (coreContext != null) {
			int tokenLocation = coreContext.getTokenLocation();
			if ((tokenLocation & CompletionContext.TL_MEMBER_START) == 0) {
				return super.computeCompletionProposals(context, monitor);
			}
		}

		if (!SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {
			return super.computeCompletionProposals(context, monitor);
		}

		ITextViewer viewer = context.getViewer();
		IDocument document = viewer.getDocument();

		try {
			int invocationOffset = context.getInvocationOffset();
			int start = invocationOffset;
			int end = invocationOffset;

			while (start != 0 && Character.isUnicodeIdentifierPart(document.getChar(start - 1))) {
				start--;
			}

			if (start < 0) {
				return super.computeCompletionProposals(context, monitor);
			}

			String prefix = document.get(start, end - start);
			if (!"findby".startsWith(prefix.toLowerCase())) {
				return super.computeCompletionProposals(context, monitor);
			}

			if (!(viewer instanceof ISourceViewer)) {
				return super.computeCompletionProposals(context, monitor);
			}

			IType expectedType = javaContext.getExpectedType();

			if (expectedType == null) {
				expectedType = javaContext.getCompilationUnit().findPrimaryType();
			}
			if (expectedType == null) {
				return super.computeCompletionProposals(javaContext, monitor);
			}
			if (!isSpringDataRepository(expectedType, javaContext.getProject())) {
				return super.computeCompletionProposals(javaContext, monitor);
			}

			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

			RepositoryInformation repositoryInfo = new RepositoryInformation(expectedType);
			Class<?> domainClass = repositoryInfo.getManagedDomainClass();
			if (domainClass != null) {
				Method[] methods = domainClass.getMethods();

				for (Method method : methods) {
					Class<?> returnType = method.getReturnType();
					if ("void".equals(returnType.getName())) {
						continue;
					}
					if (method.getParameterTypes().length > 0) {
						continue;
					}

					String methodName = method.getName();
					if (methodName.startsWith("get")) {
						String propertyName = methodName.substring(3, methodName.length());
						if ("Class".equals(propertyName)) {
							continue;
						}
						if (!containsMethodName(FindByMethodCompletionProposal.getMethodName(propertyName),
								expectedType)) {
							proposals.add(new FindByMethodCompletionProposal(propertyName, returnType, domainClass,
									start, end, javaContext));
						}
					}
				}
			}

			return proposals;
		}
		catch (BadLocationException e) {
			StatusHandler.log(new Status(Status.ERROR, DataCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(Status.ERROR, DataCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return super.computeCompletionProposals(context, monitor);
	}

	private boolean containsMethodName(String methodName, IType expectedType) {
		try {
			IMethod[] methods = expectedType.getMethods();
			for (IMethod method : methods) {
				if (method.getElementName().equals(methodName)) {
					return true;
				}
			}
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(Status.ERROR, DataCorePlugin.PLUGIN_ID, e.getMessage(), e));
		}

		return false;
	}

	private static boolean isSpringDataRepository(IType type, IJavaProject project) throws JavaModelException {
		if (type == null) {
			return false;
		}

		if (isSpringDataRepository(type)) {
			return true;
		}

		String[] interfaces = type.getSuperInterfaceNames();

		for (String extendedInterface : interfaces) {
			if (extendedInterface.contains("Repository")) {
				String[][] resolvedInterfaceTypes = type.resolveType(extendedInterface);
				if (resolvedInterfaceTypes == null) {
					continue;
				}
				for (String[] match : resolvedInterfaceTypes) {
					if (match != null && match.length == 2) {
						if (isSpringDataRepository(project.findType(match[0] + "." + match[1]), project)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private static boolean isSpringDataRepository(IType type) {
		if (RepositoryInformation.isSpringDataRepository(type)) {
			return true;
		}

		return "org.springframework.data.repository.Repository".equals(type.getFullyQualifiedName());
	}

}
