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
package org.springframework.ide.eclipse.quickfix.jdt.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.ValidationRuleUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;

/**
 * @author Terry Denney
 * @author Martin Lippert
 */
public class ProposalCalculatorUtil {

	public static boolean containsImport(ICompilationUnit cu, String importName) {
		String importPackageName = importName;
		int index = importName.lastIndexOf(".");
		if (index > 0) {
			importPackageName = importName.substring(0, index);
		}

		try {
			IPackageDeclaration[] packageDecls = cu.getPackageDeclarations();
			for (IPackageDeclaration packageDecl : packageDecls) {
				String packageName = packageDecl.getElementName();
				if (packageName.equals(importPackageName)) {
					return true;
				}

			}
			IImportDeclaration[] importDecls = cu.getImports();
			for (IImportDeclaration importDecl : importDecls) {
				String importElementName = importDecl.getElementName();
				index = importElementName.lastIndexOf(".");
				String importElementPackageName = importElementName;
				if (index > 0) {
					importElementPackageName = importElementName.substring(0, index);
				}
				if (importElementName.endsWith("*")) {
					if (importPackageName.equals(importElementPackageName)) {
						return true;
					}
				}
				else {
					if (importName.equals(importElementName)) {
						return true;
					}
				}
			}
		}
		catch (JavaModelException e) {
			SpringCore.log(e);
		}

		return false;
	}

	public static Set<Annotation> findAnnotations(String annotation, ASTNode node) {
		AnnotationFinder finder = new AnnotationFinder(annotation);
		node.accept(finder);
		return finder.getAnnotations();
	}

	public static Set<Annotation> findAnnotations(String annotation, int invocationOffset, ASTNode node) {
		AnnotationFinder finder = new AnnotationFinder(annotation, invocationOffset);
		node.accept(finder);
		return finder.getAnnotations();
	}

	public static boolean hasAnnotationInParameters(SourceMethod method, String annotationName) {
		try {
			if (method.getSource() != null) {
				return method.getSource().contains('@' + annotationName);
			}
			return false;
		}
		catch (JavaModelException e) {
			SpringCore.log(e);
			return false;
		}
	}

	public static boolean hasAnnotationOnType(ICompilationUnit cu, String annotationName) {
		try {
			IType[] allTypes = cu.getAllTypes();
			for (IType type : allTypes) {
				IAnnotation[] annotations = type.getAnnotations();
				for (IAnnotation annotation : annotations) {
					if (annotationName.equals(annotation.getElementName())) {
						return true;
					}
				}
			}
		}
		catch (JavaModelException e) {
			SpringCore.log(e);
		}
		return false;
	}

	public static boolean hasAnnotation(IAnnotatable element, String annotationName) {
		try {
			IAnnotation[] annotations = element.getAnnotations();
			for (IAnnotation annotation : annotations) {
				if (annotationName.equals(annotation.getElementName())) {
					return true;
				}
			}
		}
		catch (JavaModelException e) {
			SpringCore.log(e);
		}
		return false;
	}

	/**
	 * @param declToMatch
	 * @param problemType
	 * @param cu
	 * @param typeNameToMatch problem marker should match this typeName, set to
	 * Null if it should match any types
	 * @return
	 */
	public static ValidationProblem findProblem(BodyDeclaration declToMatch, String problemType, ICompilationUnit cu,
			String typeNameToMatch) {
		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject springProject = model.getProject(cu.getJavaProject().getProject());
		Set<IBeansConfig> configs = springProject.getConfigs();
		for (IBeansConfig config : configs) {
			AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
			provider.resolveAutowiredDependencies();
			List<ValidationProblem> problems = provider.getValidationProblems();
			for (ValidationProblem problem : problems) {
				ValidationProblemAttribute[] problemAttributes = problem.getAttributes();
				boolean matched = false;
				BodyDeclaration problemDecl = null;
				String typeName = null;

				for (ValidationProblemAttribute problemAttribute : problemAttributes) {

					if (AutowireDependencyProvider.AUTOWIRE_PROBLEM_TYPE.equals(problemAttribute.getKey())) {
						if (problemType.equals(problemAttribute.getValue())) {
							matched = true;
						}
					}
					else if ("JAVA_HANDLE".equals(problemAttribute.getKey())) {
						problemDecl = getBodyDeclaration(JavaCore.create((String) problemAttribute.getValue()));
					}
					else if (AutowireDependencyProvider.BEAN_TYPE.equals(problemAttribute.getKey())) {
						typeName = (String) problemAttribute.getValue();
					}
				}

				if (matched && problemDecl != null && problemDecl.equals(declToMatch)) {
					if (typeNameToMatch == null || (typeName != null && typeName.equals(typeNameToMatch))) {
						return problem;
					}
				}
			}
		}
		return null;
	}

	public static Set<String> getMatchingBeans(IInvocationContext context, ITypeBinding typeBinding) {
		IProject project = null;
		try {
			project = context.getCompilationUnit().getUnderlyingResource().getProject();
		}
		catch (JavaModelException e) {
			return new HashSet<String>();
		}

		String typeName = typeBinding.getQualifiedName();
		IBeansProject springProject = BeansCorePlugin.getModel().getProject(project);
		return getMatchingBeans(typeName, springProject, null);
	}

	public static Set<String> getMatchingBeans(IInvocationContext context, ITypeBinding typeBinding, String qualifier) {
		IProject project = null;
		try {
			project = context.getCompilationUnit().getUnderlyingResource().getProject();
		}
		catch (JavaModelException e) {
			return new HashSet<String>();
		}

		String typeName = typeBinding.getQualifiedName();
		IBeansProject springProject = BeansCorePlugin.getModel().getProject(project);
		return getMatchingBeans(typeName, springProject, qualifier);
	}

	public static Set<String> getMatchingBeans(JavaContentAssistInvocationContext context, ITypeBinding type) {
		IBeansProject springProject = BeansCorePlugin.getModel().getProject(context.getProject().getProject());
		String typeName = type.getQualifiedName();
		return getMatchingBeans(typeName, springProject, null);
	}

	// public static Set<String> getMatchingBeans(String typeName, IBeansProject
	// springProject) {
	// Set<String> matchingBeans = new HashSet<String>();
	//
	// Class<?> clazz = null;
	// try {
	// clazz = ClassUtils.loadClass(typeName);
	//
	// Set<IBeansConfig> configs = springProject.getConfigs();
	// for (IBeansConfig config : configs) {
	// AutowireDependencyProvider provider = new
	// AutowireDependencyProvider(config, config);
	// provider.getBeansForType(clazz);
	// }
	// }
	// catch (ClassNotFoundException e) {
	// }
	//
	// return matchingBeans;
	// }

	public static Set<String> getMatchingBeans(String typeName, IBeansProject springProject, String qualifier) {
		Set<String> matchingBeans = new HashSet<String>();
		Class<?> clazz = null;
		ClassLoader classLoader = JdtUtils.getProjectClassLoaderSupport(springProject.getProject(),
				BeansCorePlugin.getClassLoader()).getProjectClassLoader();
		try {
			clazz = classLoader.loadClass(typeName);
		}
		catch (ClassNotFoundException e) {
			return matchingBeans;
		}

		Set<IBean> beans = BeansModelUtils.getBeans(springProject);
		for (IBean bean : beans) {
			String beanClassName = ValidationRuleUtils.getBeanClassName(bean);
			if (beanClassName != null) {
				try {
					Class<?> beanClass = classLoader.loadClass(beanClassName);
					if (clazz.isAssignableFrom(beanClass)) {
						if (qualifier == null || qualifier.equals(bean.getElementName())) {
							matchingBeans.add(bean.getElementName());
						}
					}
				}
				catch (ClassNotFoundException e) {
				}
			}
		}
		return matchingBeans;
	}

	// public static Set<String> getMatchingBeans(BodyDeclaration declToMatch,
	// ICompilationUnit cu, String typeName) {
	// System.err.println("getMatchingBeans: " + typeName);
	// Set<String> matchingBeans = new HashSet<String>();
	//
	// ValidationProblem problem = findProblem(declToMatch,
	// AutowireDependencyProvider.TOO_MANY_MATCHING_BEANS, cu,
	// typeName);
	// if (problem != null) {
	// ValidationProblemAttribute[] attributes = problem.getAttributes();
	// for (ValidationProblemAttribute attribute : attributes) {
	// if
	// (attribute.getKey().startsWith(AutowireDependencyProvider.MATCHING_BEAN_NAME))
	// {
	// matchingBeans.add((String) attribute.getValue());
	// }
	// }
	// }
	//
	// return matchingBeans;
	// }

	public static String getPathVariableName(SingleVariableDeclaration param) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("PathVariable", param);
		for (Annotation annotation : annotations) {
			if (annotation.isMarkerAnnotation()) {
				return param.getName().getFullyQualifiedName();
			}
			else if (annotation.isSingleMemberAnnotation()) {
				Expression value = ((SingleMemberAnnotation) annotation).getValue();
				if (value instanceof StringLiteral) {
					return ((StringLiteral) value).getLiteralValue();
				}
			}
			else if (annotation.isNormalAnnotation()) {
				NormalAnnotation nAnnotation = (NormalAnnotation) annotation;
				@SuppressWarnings("unchecked")
				List<MemberValuePair> valuePairs = nAnnotation.values();
				for (MemberValuePair valuePair : valuePairs) {
					if ("value".equals(valuePair.getName().getIdentifier())) {
						Expression value = valuePair.getValue();
						if (value instanceof StringLiteral) {
							return ((StringLiteral) value).getLiteralValue();
						}
					}
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static MemberValuePair getRequiredMemberValuePair(BodyDeclaration decl) {
		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("Autowired", decl);
		for (Annotation annotation : annotations) {
			if (annotation instanceof NormalAnnotation) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
				List<MemberValuePair> values = normalAnnotation.values();
				for (MemberValuePair valuePair : values) {
					if ("required".equals(valuePair.getName().toString())) {
						return valuePair;
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static String getTypeName(Type type) {
		StringBuilder result = new StringBuilder();

		if (type.isArrayType()) {
			result.append(getTypeName(((ArrayType) type).getElementType()));
			result.append("[]");
		}
		else if (type.isParameterizedType()) {
			ParameterizedType pType = (ParameterizedType) type;
			result.append(getTypeName(pType.getType()));
			List<Type> typeArguments = pType.typeArguments();
			for (int i = 0; i < typeArguments.size(); i++) {
				if (i > 0) {
					result.append(", ");
				}
				result.append(getTypeName(typeArguments.get(i)));
			}
		}
		else if (type.isPrimitiveType()) {
			result.append(((PrimitiveType) type).getPrimitiveTypeCode().toString());
		}
		else if (type.isQualifiedType()) {
			QualifiedType qType = (QualifiedType) type;
			Type qualifier = qType.getQualifier();
			if (qualifier != null) {
				result.append(getTypeName(qualifier));
				result.append(".");
			}
			result.append(qType.getName().getFullyQualifiedName());
		}
		else if (type.isSimpleType()) {
			result.append(((SimpleType) type).getName().getFullyQualifiedName());
		}
		else if (type.isWildcardType()) {
			WildcardType wType = (WildcardType) type;
			result.append("? ");
			if (wType.isUpperBound()) {
				result.append("extends ");
			}
			else {
				result.append("super ");
			}
			result.append(getTypeName(wType.getBound()));
		}

		return result.toString();
	}

	@SuppressWarnings("unchecked")
	public static List<UriTemplateVariable> getUriTemplatVariables(Annotation annotation) {
		if (annotation instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation sAnnotation = (SingleMemberAnnotation) annotation;
			return getUriTemplateVariables(sAnnotation.getValue());
		}
		else if (annotation instanceof NormalAnnotation) {
			NormalAnnotation nAnnotation = (NormalAnnotation) annotation;
			List<MemberValuePair> pairs = nAnnotation.values();
			for (MemberValuePair pair : pairs) {
				if ("value".equals(pair.getName().getFullyQualifiedName())) {
					return getUriTemplateVariables(pair.getValue());
				}
			}
		}

		return new ArrayList<UriTemplateVariable>();
	}

	/**
	 * @param annotation to be matched, or null if matches all annotations
	 * @param node
	 * @return true if there is a matching annotation on the node
	 */
	public static boolean hasAnnotation(String annotation, ASTNode node) {
		AnnotationFinder finder = new AnnotationFinder(annotation);
		node.accept(finder);
		Set<Annotation> annotations = finder.getAnnotations();
		return !annotations.isEmpty();
	}

	public static boolean hasProblem(BodyDeclaration declToMatch, String problemType, ICompilationUnit cu) {
		return findProblem(declToMatch, problemType, cu, null) != null;
	}

	private static Class<?>[] KNOWN_REQUEST_MAPPING_PARAM_TYPE = new Class<?>[] { InputStream.class,
			OutputStream.class, Reader.class, Writer.class, Principal.class, Locale.class, HttpServletRequest.class,
			HttpServletResponse.class, HttpSession.class };

	public static boolean isKnownRequestMappingParamType(IProject project, ITypeBinding typeBinding) {
		String typeName = typeBinding.getQualifiedName();
		ClassLoader classLoader = JdtUtils.getProjectClassLoaderSupport(project, BeansCorePlugin.getClassLoader())
				.getProjectClassLoader();
		try {
			Class<?> clazz = classLoader.loadClass(typeName);

			for (Class<?> knownClass : KNOWN_REQUEST_MAPPING_PARAM_TYPE) {
				Class<?> loadedClass = classLoader.loadClass(knownClass.getCanonicalName());
				if (loadedClass.isAssignableFrom(clazz)) {
					return true;
				}
			}
		}
		catch (ClassNotFoundException e) {
		}

		return false;
	}

	private static BodyDeclaration getBodyDeclaration(IJavaElement element) {
		try {
			if (element instanceof IMember) {
				IMember member = (IMember) element;
				ICompilationUnit compilationUnit = member.getCompilationUnit();
				ISourceRange sourceRange = member.getSourceRange();
				AssistContext assistContext = new AssistContext(compilationUnit, null, sourceRange.getOffset(),
						sourceRange.getLength());
				ASTNode node = assistContext.getCoveringNode();
				if (node instanceof BodyDeclaration) {
					return (BodyDeclaration) node;
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	private static List<UriTemplateVariable> getUriTemplateVariables(Expression expression) {
		List<UriTemplateVariable> variables = new ArrayList<UriTemplateVariable>();

		if (expression instanceof StringLiteral) {
			StringLiteral literal = (StringLiteral) expression;
			String uriTemplate = literal.getLiteralValue();
			int offset = 1; // skip start quote

			while (uriTemplate.length() > 0) {
				int index = uriTemplate.indexOf("{");
				if (index < 0) {
					break;
				}

				uriTemplate = uriTemplate.substring(index + 1);
				offset += index + 1;

				index = uriTemplate.indexOf(":");
				if (index < 0) {
					index = uriTemplate.indexOf("}");
					if (index < 0) {
						break;
					}
				}

				variables.add(new UriTemplateVariable(uriTemplate.substring(0, index), offset, literal));
				uriTemplate = uriTemplate.substring(index + 1);
				offset += index + 1;
			}
		}

		return variables;
	}

	private static class AnnotationFinder extends ASTVisitor {

		Set<Annotation> annotations = new HashSet<Annotation>();

		private final String annotationToMatch;

		private final int invocationOffset;

		private boolean isTypeDecl;

		public AnnotationFinder(String annotationToMatch) {
			this.annotationToMatch = annotationToMatch;
			this.invocationOffset = -1;
		}

		public AnnotationFinder(String annotationToMatch, int invocationOffset) {
			this.annotationToMatch = annotationToMatch;
			this.invocationOffset = invocationOffset;

		}

		public Set<Annotation> getAnnotations() {
			return annotations;
		}

		@Override
		public boolean visit(FieldDeclaration node) {
			if (isTypeDecl) {
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(MarkerAnnotation node) {
			if (matches(node)) {
				annotations.add(node);
			}
			return false;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			if (isTypeDecl) {
				return false;
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(NormalAnnotation node) {
			if (matches(node)) {
				annotations.add(node);
			}
			return false;
		}

		@Override
		public boolean visit(SingleMemberAnnotation node) {
			if (matches(node)) {
				annotations.add(node);
			}
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration typeDecl) {
			isTypeDecl = true;
			return super.visit(typeDecl);
		}

		private boolean matches(Annotation annotation) {
			Name typeName = annotation.getTypeName();
			if (typeName != null) {
				if (invocationOffset >= 0) {
					int startPos = annotation.getStartPosition();
					if (startPos > invocationOffset || startPos + annotation.getLength() < invocationOffset) {
						return false;
					}
				}
				return annotationToMatch == null || typeName.toString().equals(annotationToMatch);
			}
			return false;
		}
	}

}
