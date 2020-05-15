package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.model.AbstractDisposable;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;
import org.springsource.ide.eclipse.commons.livexp.util.Log;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class DockerDeployments extends AbstractDisposable {

	private static final String PERSISTENCE_KEY = DockerDeployments.class.getName();
	private final Map<String, DockerDeployment> byName = new HashMap<>();
	
	ObservableSet<DockerDeployment> deployments = new ObservableSet<DockerDeployment>() {
		@Override
		protected ImmutableSet<DockerDeployment> compute() {
			return ImmutableSet.copyOf(byName.values());
		}
	};
	
	public DockerDeployments(PropertyStoreApi persistentProperties) {
		{
			Yaml yaml = yaml();
			String serialized = persistentProperties.get(PERSISTENCE_KEY);
			try {
				if (StringUtils.hasText(serialized)) {
					DockerDeploymentList deserialized = yaml.loadAs(serialized, DockerDeploymentList.class);
					for (DockerDeployment d : deserialized.getDeployments()) {
						byName.put(d.getName(), d);
					}
					deployments.refresh();
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
		deployments.onChange(this, (_e, _v) -> {
			Yaml yaml = yaml();
			String serialized = yaml.dump(new DockerDeploymentList(deployments.getValues()));
			try {
				persistentProperties.put(PERSISTENCE_KEY, serialized);
			} catch (Exception e) {
				Log.log(e);
			}
		});
	}

	private Yaml yaml() {
		return new Yaml(new CustomClassLoaderConstructor(DockerDeploymentList.class, DockerDeploymentList.class.getClassLoader()));
	}
	
	public void createOrUpdate(DockerDeployment deployment) {
		byName.put(deployment.getName(), deployment);
		deployments.refresh();
	}

	public void remove(String name) {
		byName.remove(name);
		deployments.refresh();
	}

	public ObservableSet<DockerDeployment> getDeployments() {
		return deployments;
	}
}
