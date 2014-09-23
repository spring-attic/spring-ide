package org.springframework.ide.eclipse.propertiesfileeditor;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.springframework.boot.config.ConfigMetadataRepository;
import org.springframework.boot.config.processor.mapper.ConfigMetadataRepositoryJsonMapper;

/**
 * Load a {@link ConfigMetadataRepository} from the content of an eclipse
 * projects classpath.
 * <p>
 * Loosely based on the class of the same name in spring-boot (
 * org.springframework.boot.config.support.ConfigMetadataRepositoryJsonLoader.ConfigMetadataRepositoryJsonLoader())
 *
 * @author Kris De Volder
 * @since 1.2.0
 */
public class ConfigMetadataRepositoryJsonLoader {

	/**
	 * The default classpath location for config metadata.
	 */
	public static final String DEFAULT_LOCATION_PATTERN =
			"META-INF/boot/config-metadata.json";

	/**
	 * The default classpath location for manual config metadata.
	 */
	public static final String DEFAULT_MANUAL_LOCATION_PATTERN =
			"META-INF/boot/config-manual-metadata.json";

	private ConfigMetadataRepositoryJsonMapper mapper = new ConfigMetadataRepositoryJsonMapper();

	/**
	 * Load the {@link ConfigMetadataRepository} with the metadata of the current
	 * classpath using the {@link #DEFAULT_LOCATION_PATTERN}. If the same config
	 * metadata items is held within different resources, the first that is
	 * loaded is kept which means the result is not deterministic.
	 */
	public ConfigMetadataRepository load(IJavaProject project) throws Exception {
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		for (IPackageFragmentRoot pfr : roots) {
			IResource rsrc = pfr.getUnderlyingResource();
			System.out.println("pfr = "+rsrc);
		}
		throw new Error("Not implemented");
	}

//	/**
//	 * Load the {@link ConfigMetadataRepository} with the metadata defined by
//	 * the specified {@code resources}. If the same config metadata items is
//	 * held within different resources, the first that is loaded is kept.
//	 */
//	public ConfigMetadataRepository load(Collection<IFile> resources) throws IOException {
//		Assert.notNull(resources, "Resources must not be null");
//		if (resources.size() == 1) {
//			return load(resources.iterator().next());
//		}
//
//		SimpleConfigMetadataRepository repository = new SimpleConfigMetadataRepository();
//		for (IResource resource : resources) {
//			ConfigMetadataRepository repo = load(resource);
//			repository.include(repo);
//		}
//		return repository;
//	}
//
//	private ConfigMetadataRepository load(Resource resource) throws IOException {
//		InputStream in = resource.getInputStream();
//		try {
//			return mapper.readRepository(in);
//		}
//		catch (IOException e) {
//			throw new IllegalStateException("Failed to read config metadata from '" + resource + "'", e);
//		}
//		catch (JSONException e) {
//			throw new IllegalStateException("Invalid config metadata document defined at '" + resource + "'", e);
//		}
//
//		finally {
//			in.close();
//		}
//	}

}
