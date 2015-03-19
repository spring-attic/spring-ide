package org.springframework.ide.eclipse.boot.validation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

public class MissingConfigurationProcessor implements IValidationRule<CompilationUnit, SpringBootValidationContext> {

	@Override
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof CompilationUnit;
	}

	@Override
	public void validate(CompilationUnit cu, SpringBootValidationContext context, IProgressMonitor monitor) {
		if (cu.getElementName().toLowerCase().contains("foo")) {
			context.addProblems(new ValidationProblem(IMarker.SEVERITY_WARNING, "Using 'foo' in your names is so last year", cu.getElementResource()));
		}
	}

}
