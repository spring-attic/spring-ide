package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.ISourceViewer;
import org.springframework.ide.eclipse.data.jdt.core.RepositoryInformation;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.FindByMethodCompletionProposal;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Terry Denney
 * 
 * Completion proposal computer for adding template methods for findBy...
 * methods.
 * 
 */
public class FindByMethodCompletionProposalComputer extends JavaCompletionProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		ITextViewer viewer = context.getViewer();
		IDocument document = viewer.getDocument();

		try {
			int invocationOffset = context.getInvocationOffset();
			int start = invocationOffset;
			int end = invocationOffset;

			while (start != 0 && Character.isUnicodeIdentifierPart(document.getChar(start - 1))) {
				start--;
			}

			if (start >= 0) {
				String prefix = document.get(start, end - start);
				if ("findby".startsWith(prefix.toLowerCase())) {
					if (viewer instanceof ISourceViewer && context instanceof JavaContentAssistInvocationContext) {
						JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
						if (!SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {
							return super.computeCompletionProposals(context, monitor);
						}

						AssistContext assistContext = new AssistContext(javaContext.getCompilationUnit(),
								(ISourceViewer) viewer, start, end - start);

						ASTNode coveredNode = assistContext.getCoveredNode();
						ASTNode coveringNode = assistContext.getCoveringNode();

						FieldDeclaration stubFieldDecl = null;
						TypeDeclaration typeDecl = null;

						if (coveredNode == null && coveringNode instanceof TypeDeclaration) {
							typeDecl = (TypeDeclaration) coveringNode;
						}
						else {
							if (coveredNode instanceof SimpleName) {
								ASTNode parent = coveredNode.getParent();
								if (parent instanceof SimpleType) {
									parent = parent.getParent();
									if (parent instanceof FieldDeclaration) {
										FieldDeclaration fieldDecl = (FieldDeclaration) parent;
										@SuppressWarnings("unchecked")
										List<VariableDeclarationFragment> fragments = fieldDecl.fragments();
										if (fragments.isEmpty() || fragments.size() == 1) {
											typeDecl = getParentTypeDeclaration(fieldDecl);
											if (typeDecl != null) {
												@SuppressWarnings("unchecked")
												List<BodyDeclaration> bodyDecls = typeDecl.bodyDeclarations();
												BodyDeclaration nextSibling = null;
												for (int i = 0; i < bodyDecls.size(); i++) {
													BodyDeclaration bodyDecl = bodyDecls.get(i);
													if (bodyDecl == fieldDecl && i + 1 < bodyDecls.size()) {
														nextSibling = bodyDecls.get(i + 1);
														break;
													}
												}

												if (nextSibling instanceof MethodDeclaration) {
													if (((MethodDeclaration) nextSibling).getReturnType2() == null) {
														stubFieldDecl = fieldDecl;
													}
												}
											}
										}
									}
								}
							}

							if (stubFieldDecl == null) {
								return super.computeCompletionProposals(context, monitor);
							}
						}

						List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

						IType expectedType = javaContext.getExpectedType();

						if (expectedType == null) {
							expectedType = javaContext.getCompilationUnit().findPrimaryType();
						}

						if (expectedType != null || stubFieldDecl != null
								&& isSpringDataRepository(expectedType, javaContext.getProject())) {
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
										if (!containsMethodName(
												FindByMethodCompletionProposal.getMethodName(propertyName),
												getParentTypeDeclaration(coveringNode))) {
											proposals.add(new FindByMethodCompletionProposal(propertyName, returnType,
													domainClass, typeDecl, start, end, javaContext, stubFieldDecl));
										}
									}
								}
							}
						}

						return proposals;
					}
				}
			}
		}
		catch (BadLocationException e) {
			StatusHandler.log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return super.computeCompletionProposals(context, monitor);
	}

	private TypeDeclaration getParentTypeDeclaration(ASTNode node) {
		if (node instanceof TypeDeclaration || node == null) {
			return (TypeDeclaration) node;
		}
		return getParentTypeDeclaration(node.getParent());
	}

	private boolean containsMethodName(String methodName, TypeDeclaration typeDecl) {
		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyDecls = typeDecl.bodyDeclarations();
		for (BodyDeclaration bodyDecl : bodyDecls) {
			if (bodyDecl instanceof MethodDeclaration) {
				MethodDeclaration methodDecl = (MethodDeclaration) bodyDecl;
				if (methodDecl.getName().getIdentifier().equals(methodName)) {
					return true;
				}
			}
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
