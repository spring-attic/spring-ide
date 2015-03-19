package org.springframework.ide.eclipse.boot.validation;

public interface IBootModelElementTypes {
	/**
	 * Constant representing a compilation unit. A model element with this type
	 * can be safely cast to {@link CompilationUnit}.
	 */
	int COMPILATION_UNIT_TYPE = 2;
	// starts with 2 because 1 is reserved for the model
}
