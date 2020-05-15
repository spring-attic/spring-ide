package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import org.springframework.ide.eclipse.boot.dash.model.Nameable;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Data class containing info about a 'deployment' (i.e. what gets created when you
 * drag and drop a project onto a docker target. 
 * <p>
 * This is similar in concept to a k8s deployment. It is an object that describes a
 * indented state of some containers running in docker. It contains all the
 * information needed to create the container.
 * <p>
 * This object must be easy to serialize as it needs to be persisted across Eclipse
 * restarts.
 */
public class DockerDeployment implements Nameable {
	
	private String name; // name of the deployment, currently this is also the name of the deployed project.
	private RunState runState;
	private String buildId;
	
	public DockerDeployment() {}

	public DockerDeployment(DockerDeployment copyFrom) {
		this.name = copyFrom.name;
		this.runState = copyFrom.runState;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public RunState getRunState() {
		return runState;
	}
	public void setRunState(RunState runState) {
		this.runState = runState;
	}

	public DockerDeployment withGoalState(RunState newGoalState) {
		DockerDeployment d = new DockerDeployment(this);
		d.setRunState(newGoalState);
		return d;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}
}
