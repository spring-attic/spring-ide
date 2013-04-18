package org.springframework.ide.eclipse.wizard.template;

/**
 * Describes the type of Spring project that the New Spring Project wizard
 * should create.
 * 
 */
public class ProjectWizardDescriptor {

	static enum ProjectType {
		SIMPLE(), TEMPLATE()
	}

	static enum BuildType {
		Maven, Java
	}

	private final ProjectType projectType;

	private final BuildType buildType;

	public ProjectWizardDescriptor(ProjectType projectType, BuildType buildType) {
		this.projectType = projectType;
		this.buildType = buildType;
	}

	public ProjectType getProjectType() {
		return projectType;
	}

	public BuildType getBuildType() {
		return buildType;
	}

}
