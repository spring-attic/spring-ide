/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation;

import static org.springframework.ide.eclipse.boot.quickfix.GeneratorComposition.NO_RESOLUTIONS;
import static org.springframework.ide.eclipse.boot.validation.BootMarkerUtils.getProject;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.quickfix.MarkerResolutionRegistry;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * Validation rule that checks
 *
 *   if: found @ConfigurationProperties annotation
 *   then: spring-boot-configuration-processor jar is on the project's classpath
 *
 * Provides a quickfix to add spring-boot-configuration-processor to dependencies in
 * pom-based project.
 *
 * @author Kris De Volder
 */
public class MissingConfigurationProcessorRule extends BootValidationRule {

	private static final String PROBLEM_ID = MissingConfigurationProcessorRule.class.getName();
	private static final MavenCoordinates DEP_CONFIGURATION_PROCESSOR =
			new MavenCoordinates("org.springframework.boot", "spring-boot-configuration-processor", null);

	private static final IMarkerResolutionGenerator2 QUICK_FIX = new IMarkerResolutionGenerator2() {

		@Override
		public IMarkerResolution[] getResolutions(IMarker marker) {
			try {
				final ISpringBootProject project = SpringBootCore.create(getProject(marker));
				return new IMarkerResolution[] {
						new IMarkerResolution() {
							@Override
							public String getLabel() {
								return "Add spring-boot-configuration-processor to pom.xml";
							}

							@Override
							public void run(IMarker marker) {
								try {
									project.addMavenDependency(DEP_CONFIGURATION_PROCESSOR, true);
									project.updateProjectConfiguration(); //needed to actually enable APT, m2e does not
															// automatically trigger this if a dependency gets added
								} catch (Exception e) {
									BootActivator.log(e);
								}
							}
						}
				};
			} catch (Exception e) {
				return NO_RESOLUTIONS;
			}
		}

		@Override
		public boolean hasResolutions(IMarker marker) {
			try {
				IProject project = getProject(marker);
				if (project.hasNature(SpringBootCore.M2E_NATURE)) {
					return true;
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return false;
		}
	};

	static {
		MarkerResolutionRegistry.DEFAULT_INSTANCE.register(PROBLEM_ID, QUICK_FIX);
	}

	/**
	 * Classpath matcher that checks classpath to determine if this rule applies
	 */
	private static final ClasspathMatcher CLASSPATH_MATCHER = new ClasspathMatcher(false) {
		@Override
		protected boolean doMatch(IClasspathEntry[] classpath) {
			for (IClasspathEntry e : classpath) {
				if (isJarNameContaining(e, "spring-boot-configuration-processor")) {
					//The rules is already satisfied so doesn't need to be checked
					return false;
				}
			}
			return true;
		}
	};

	public static class ValidationVisitor {

		private SpringBootValidationContext context;
		private SpringCompilationUnit cu;

		public ValidationVisitor(SpringBootValidationContext context, SpringCompilationUnit cu) {
			this.context = context;
			this.cu = cu;
		}

		public void visit(ICompilationUnit compilationUnit, IProgressMonitor mon) throws Exception {
			IType[] types = compilationUnit.getAllTypes();
			mon.beginTask(compilationUnit.getElementName(), types.length);
			try {
				for (IType t : types) {
					visit(t, new SubProgressMonitor(mon, 1));
				}
			} finally {
				mon.done();
			}
		}

		private void visit(IType t, SubProgressMonitor mon) throws Exception {
			IMethod[] methods = t.getMethods();
			mon.beginTask(t.getElementName(), 1+methods.length);
			try {
				IAnnotation annot = t.getAnnotation("ConfigurationProperties");
				if (annot!=null && annot.exists()) {
					visit(annot);
					mon.worked(1);
				}
				for (IMethod m : methods) {
					visit(m, new SubProgressMonitor(mon, 1));
				}
			} finally {
				mon.done();
			}
		}

		private void visit(IAnnotation annot) throws Exception {
			warn("When using @ConfigurationProperties it is recommended to add 'spring-boot-configuration-processor' "
					+ "to your classpath to generate configuration metadata", annot.getNameRange());
		}

		private void visit(IMethod m, SubProgressMonitor mon) throws Exception {
			mon.beginTask(m.getElementName(), 1);
			try {
				IAnnotation annot = m.getAnnotation("ConfigurationProperties");
				if (annot!=null && annot.exists()) {
					visit(annot);
					mon.worked(1);
				}
			} finally {
				mon.done();
			}
		}

		void warn(String msg, ISourceRange location) {
			if (location!=null) {
				context.addProblems(new ValidationProblem(PROBLEM_ID, IMarker.SEVERITY_WARNING,
						msg, cu.getElementResource(), location));
			}
		}

	}

	@Override
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof SpringCompilationUnit;
	}

	@Override
	public void validate(SpringCompilationUnit cu, SpringBootValidationContext context, IProgressMonitor mon) {
		try{
			if (CLASSPATH_MATCHER.match(cu.getClasspath())) {
				ValidationVisitor visitor = new ValidationVisitor(context, cu);
				visitor.visit(cu.getCompilationUnit(), mon);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

}
