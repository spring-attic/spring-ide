package org.springframework.ide.eclipse.boot.core.cli;

import java.io.File;

import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;

public class BootCliUtils {
	
	private static final String CLOUD_CLI_LIB_PREFIX = "spring-cloud-cli";
	private static final String CLOUD_DEPLOYER_CLI_LIB_PREFIX = "spring-cloud-launcher-cli";
	
	public static IBootInstall getSpringBootInstall() throws Exception {
		return BootInstallManager.getInstance().getDefaultInstall();
	}
	
	public static File getSpringBootHome(IBootInstall install) throws Exception {
		return install.getHome();
	}
	
	public static File getSpringBootHome() throws Exception {
		return getSpringBootHome(getSpringBootInstall());
	}

	public static boolean supportsSpringCloud(IBootInstall install) {
		boolean cloudCliFound = false, cloudDeployerCliFound = false;
		try {
			for (File lib : install.getExtensionsJars()) {
				if (lib.getName().startsWith(CLOUD_CLI_LIB_PREFIX)) {
					cloudCliFound = true;
				}
				if (lib.getName().startsWith(CLOUD_DEPLOYER_CLI_LIB_PREFIX)) {
					cloudDeployerCliFound = true;
				}
				if (cloudCliFound && cloudDeployerCliFound) {
					return true;
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return cloudCliFound && cloudDeployerCliFound;
	}
}
