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
package org.springframework.roo.addon.roobot.eclipse.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.roobot.client.model.Bundle;
import org.springframework.roo.addon.roobot.client.model.BundleVersion;
import org.springframework.roo.addon.roobot.client.model.Comment;
import org.springframework.roo.addon.roobot.client.model.Rating;
import org.springframework.roo.felix.BundleSymbolicName;
import org.springframework.roo.felix.pgp.PgpKeyId;
import org.springframework.roo.felix.pgp.PgpService;
import org.springframework.roo.shell.Shell;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.springframework.roo.uaa.UaaRegistrationService;
import org.springframework.roo.url.stream.UrlInputStreamService;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.Plugin;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginService.InstallOrUpgradeStatus;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins.PluginVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implementation of commands that are available via the Roo shell.
 * 
 * @author Stefan Schmidt
 * @author Ben Alex
 * @since 1.1
 */
@Component
@Service
public class AddOnRooBotEclipseOperationsImpl implements AddOnRooBotEclipseOperations {

	private Map<String, Bundle> bundleCache;
	@Reference private Shell shell;
	@Reference private PgpService pgpService;
	@Reference private UrlInputStreamService urlInputStreamService;
	private static final Logger log = Logger.getLogger(AddOnRooBotEclipseOperationsImpl.class.getName());
	private Properties props;
	private ComponentContext context;
	private static String ROOBOT_XML_URL = "http://spring-roo-repository.springsource.org/roobot/roobot.xml.zip";
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private final Class<AddOnRooBotEclipseOperationsImpl> mutex = AddOnRooBotEclipseOperationsImpl.class;
//	private Preferences prefs;
	
	public static final String ADDON_UPGRADE_STABILITY_LEVEL = "ADDON_UPGRADE_STABILITY_LEVEL";
	
	protected void activate(ComponentContext context) {
		this.context = context;
		//prefs = Preferences.userNodeForPackage(AddOnRooBotEclipseOperationsImpl.class);
		bundleCache = new HashMap<String, Bundle>();
		Thread t = new Thread(new Runnable() {
			public void run() {
				synchronized (mutex) {
					populateBundleCache(true);
				}
			}
		}, "Spring Roo RooBot Add-In Index Eager Download");
		t.start();
		props = new Properties();
		try {
			props.load(FileUtils.getInputStream(getClass(), "manager.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean trust(PluginVersion pluginVersion) {
		BundleVersion bundleVersion = ((RooAddOnVersion)pluginVersion).getBundleVersion();
		pgpService.trust(new PgpKeyId(bundleVersion.getPgpKey()));
		return true;
	}
	
	public InstallOrUpgradeStatus installOrUpgradeAddOn(PluginVersion pluginVersion, boolean install) {
		synchronized (mutex) {
		BundleVersion bundleVersion = ((RooAddOnVersion)pluginVersion).getBundleVersion();
		Bundle bundle = ((RooAddOnVersion)pluginVersion).getBundle();
		if (!verifyRepository(bundleVersion.getObrUrl())) {
			return InstallOrUpgradeStatus.INVALID_REPOSITORY_URL;
		}
		boolean success = true;	
		int count = countBundles();
		boolean requiresWrappedCoreDep = bundleVersion.getDescription().contains("#wrappedCoreDependency");
		if (requiresWrappedCoreDep && !shell.executeCommand("osgi obr url add --url http://spring-roo-repository.springsource.org/repository.xml")) {
			success = false;
		}
		if (!shell.executeCommand("osgi obr url add --url " + bundleVersion.getObrUrl())) {
			success = false;
		}
		if (!shell.executeCommand("osgi obr start --bundleSymbolicName " + bundle.getSymbolicName())) {
			success = false;
		}
		if (!shell.executeCommand("osgi obr url remove --url " + bundleVersion.getObrUrl())) {
			success = false;
		}
		if (requiresWrappedCoreDep && !shell.executeCommand("osgi obr url remove --url http://spring-roo-repository.springsource.org/repository.xml")) {
			success = false;
		}
		if (install && count == countBundles()) {
			return InstallOrUpgradeStatus.VERIFICATION_NEEDED; // most likely PgP verification required before the bundle can be installed, no log needed
		}

		if (success) {
			return InstallOrUpgradeStatus.SUCCESS;
		} else {
			return InstallOrUpgradeStatus.FAILED;
		}
		}
	}

	public InstallOrUpgradeStatus removeAddOn(PluginVersion pluginVersion) {
		Bundle bundle = ((RooAddOnVersion)pluginVersion).getBundle();
		BundleSymbolicName bsn = new BundleSymbolicName(bundle.getSymbolicName());
		synchronized (mutex) {
			Assert.notNull(bsn, "Bundle symbolic name required");
			boolean success = false;
			int count = countBundles();
			success = shell.executeCommand("osgi uninstall --bundleSymbolicName " + bsn.getKey());
			if (count == countBundles() || !success) {
				return InstallOrUpgradeStatus.FAILED;
			} else {
				return InstallOrUpgradeStatus.SUCCESS;
			}
		}
	}
	
	public List<Plugin> searchAddOns(String searchTerms, boolean refresh, boolean trustedOnly, boolean compatibleOnly, String requiresCommand) {
		synchronized (mutex) {
			if (bundleCache.size() == 0) {
				// We should refresh regardless in this case
				refresh = true;
			}
			if (refresh && populateBundleCache(false)) {
			}
			if (bundleCache.size() != 0) {
				boolean onlyRelevantBundles = false;
				if (searchTerms != null && !"".equals(searchTerms)) {
					onlyRelevantBundles = true;
					String [] terms = searchTerms.split(",");
					for (Bundle bundle: bundleCache.values()) {
						//first set relevance of all bundles to zero
						bundle.setSearchRelevance(0f);
						int hits = 0;
						BundleVersion latest = bundle.getLatestVersion();
						for (String term: terms) {
							if ((bundle.getSymbolicName() + ";" + latest.getSummary()).toLowerCase().contains(term.trim().toLowerCase()) || term.equals("*")) {
								hits++;
							}
						}
						bundle.setSearchRelevance(hits / terms.length);
					}
				}
				List<Bundle> bundles = Bundle.orderBySearchRelevance(new ArrayList<Bundle>(bundleCache.values()));
				LinkedList<Bundle> filteredSearchResults = filterList(bundles, trustedOnly, compatibleOnly, requiresCommand, onlyRelevantBundles);
				return convertToAddOns(filteredSearchResults);
			}
			return null;
		}
	}

	private List<Plugin> convertToAddOns(
			LinkedList<Bundle> filteredSearchResults) {
		BundleContext bc = context.getBundleContext();
		org.osgi.framework.Bundle[] bundles = bc.getBundles();
		Map<String, org.osgi.framework.Bundle> installedBundleBySymbolicName = new HashMap<String, org.osgi.framework.Bundle>();
		for (org.osgi.framework.Bundle bundle : bundles) {
			installedBundleBySymbolicName.put(bundle.getSymbolicName(), bundle);
		}
		
		ArrayList<Plugin> result = new ArrayList<Plugin>();
		for (Bundle bundle : filteredSearchResults) {
			org.osgi.framework.Bundle installedBundle = installedBundleBySymbolicName.get(bundle.getSymbolicName());
			
			// create add-on for each bundle
			Plugin plugin = new Plugin(bundle.getSymbolicName());
			
			// add all available versions
			List<BundleVersion> versions = BundleVersion.orderByVersion(bundle.getVersions());
			for (BundleVersion version : versions) {
				RooAddOnVersion addOnVersion = new RooAddOnVersion(bundle,
						version);
				addOnVersion.setTitle(version.getPresentationName());
				addOnVersion.setVersion(version.getVersion());
				addOnVersion.setDescription(version.getDescription());
				addOnVersion.setRuntimeVersion(version.getRooVersion());
				// name needs to match between bundle and version
				addOnVersion.setName(plugin.getName());
				
				if (installedBundle != null && installedBundle.getVersion().toString().equals(version.getVersion())) {
					addOnVersion.setInstalled(true);
				}
				
				plugin.addVersion(addOnVersion);
				plugin.setLatestReleasedVersion(addOnVersion);
			}
			
			result.add(plugin);
		}
		return result;
	}

//	public void upgradeSettings(AddOnStabilityLevel addOnStabilityLevel) {
//		if (addOnStabilityLevel == null) {
//			addOnStabilityLevel = checkAddOnStabilityLevel(addOnStabilityLevel);
//			log.info("Current Add-on Stability Level: " + addOnStabilityLevel.name());
//		} else {
//			boolean success = true;
//			prefs.putInt(ADDON_UPGRADE_STABILITY_LEVEL, addOnStabilityLevel.getLevel());
//			try {
//				prefs.flush();
//			} catch (BackingStoreException ignore) {
//				success = false;
//			}
//			if (success) {
//				log.info("Add-on Stability Level: " + addOnStabilityLevel.name() + " stored");
//			} else {
//				log.warning("Unable to store add-on stability level at this time");
//			}
//		}
//	}
		
	public Map<String, Bundle> getAddOnCache(boolean refresh) {
		synchronized (mutex) {
			if (refresh) {
				populateBundleCache(false);
			}
			return Collections.unmodifiableMap(bundleCache);
		}
	}
	
	private LinkedList<Bundle> filterList(List<Bundle> bundles, boolean trustedOnly, boolean compatibleOnly, String requiresCommand, boolean onlyRelevantBundles) {
		LinkedList<Bundle> filteredList = new LinkedList<Bundle>();
		List<PGPPublicKeyRing> keys = null;
		if (trustedOnly) {
			keys = pgpService.getTrustedKeys();
		}
		bundle_loop: for (Bundle bundle: bundles) {
			BundleVersion latest = bundle.getLatestVersion();
			if (onlyRelevantBundles && !(bundle.getSearchRelevance() > 0)) {
				continue bundle_loop;
			}
			if (trustedOnly && !isTrustedKey(keys, latest.getPgpKey())) {
				continue bundle_loop;
			} 
			if (compatibleOnly && !isCompatible(latest.getRooVersion())) {
				continue bundle_loop;
			}
			if (requiresCommand != null && requiresCommand.length() > 0) {
				boolean matchingCommand = false;
				for (String cmd : latest.getCommands().keySet()) {
					if (cmd.startsWith(requiresCommand) || requiresCommand.startsWith(cmd)) {
						matchingCommand = true;
						break;
					}
				}
				if (!matchingCommand) {
					continue bundle_loop;
				}
			}
			filteredList.add(bundle);
		}
		return filteredList;
	}
	
	@SuppressWarnings("unchecked")
	private boolean isTrustedKey(List<PGPPublicKeyRing> keys, String keyId) {
		for (PGPPublicKeyRing keyRing: keys) {
			Iterator<PGPPublicKey> it = keyRing.getPublicKeys();
			while (it.hasNext()) {
				PGPPublicKey pgpKey = (PGPPublicKey) it.next();
				if (new PgpKeyId(pgpKey).equals(new PgpKeyId(keyId))) { 
					return true;
				}
			}
		}
		return false;
	}

	private boolean populateBundleCache(boolean startupTime) {
		boolean success = false;
		InputStream is = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			String url = props.getProperty("roobot.url", ROOBOT_XML_URL);
			if (url == null) {
				log.warning("Bundle properties could not be loaded");
				return false;
			}
			if (url.startsWith("http://")) {
				// Handle it as HTTP
				URL httpUrl = new URL(url);
				String failureMessage = urlInputStreamService.getUrlCannotBeOpenedMessage(httpUrl);
				if (failureMessage != null) {
					if (!startupTime) {
						// This wasn't just an eager startup time attempt, so let's display the error reason
						// (for startup time, we just fail quietly)
						log.warning(failureMessage);
					}
					return false;
				}
				// It appears we can acquire the URL, so let's do it
				is = urlInputStreamService.openConnection(httpUrl);
			} else {
				// Fallback to normal protocol handler (likely in local development testing etc
				is = new URL(url).openStream();
			}
			if (is == null) {
				log.warning("Could not connect to Roo Addon bundle repository index");
				return false;
			}
			
			ZipInputStream zip = new ZipInputStream(is);
			zip.getNextEntry();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[8192];
			int length = -1;
			while (zip.available() > 0) {
				length = zip.read(buffer, 0, 8192);
				if (length > 0) {
					baos.write(buffer, 0, length);
				}
			}
			
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			Document roobotXml = db.parse(bais);
			
			if (roobotXml != null) {
				bundleCache.clear();
				for (Element bundleElement : XmlUtils.findElements("/roobot/bundles/bundle", roobotXml.getDocumentElement())) {
					String bsn = bundleElement.getAttribute("bsn");
					List<Comment> comments = new LinkedList<Comment>();
					for (Element commentElement: XmlUtils.findElements("comments/comment", bundleElement)) {
						comments.add(new Comment(Rating.fromInt(new Integer(commentElement.getAttribute("rating"))), commentElement.getAttribute("comment"), dateFormat.parse(commentElement.getAttribute("date"))));
					}
					Bundle bundle = new Bundle(bundleElement.getAttribute("bsn"), new Float(bundleElement.getAttribute("uaa-ranking")).floatValue(), comments);
						
					for (Element versionElement: XmlUtils.findElements("versions/version", bundleElement)) {
						if (bsn != null && bsn.length() > 0 && versionElement != null) {
							String signedBy = "";
							String pgpKey = versionElement.getAttribute("pgp-key-id");
							if (pgpKey != null && pgpKey.length() > 0) {
								Element pgpSigned = XmlUtils.findFirstElement("/roobot/pgp-keys/pgp-key[@id='" + pgpKey + "']/pgp-key-description", roobotXml.getDocumentElement());
								if (pgpSigned != null) {
									signedBy = pgpSigned.getAttribute("text");
								}
							}
							
							Map<String, String> commands = new HashMap<String, String>();
							for (Element shell : XmlUtils.findElements("shell-commands/shell-command", versionElement)) {
								commands.put(shell.getAttribute("command"), shell.getAttribute("help"));
							}
							
							StringBuilder versionBuilder = new StringBuilder();
							versionBuilder.append(versionElement.getAttribute("major")).append(".").append(versionElement.getAttribute("minor"));
							String versionMicro = versionElement.getAttribute("micro");
							if (versionMicro != null && versionMicro.length() > 0) {
								versionBuilder.append(".").append(versionMicro);
							}
							String versionQualifier = versionElement.getAttribute("qualifier");
							if (versionQualifier != null && versionQualifier.length() > 0) {
								versionBuilder.append(".").append(versionQualifier);
							}
							
							String rooVersion = versionElement.getAttribute("roo-version");
							if (rooVersion.equals("*") || rooVersion.length() == 0) {
								rooVersion = getVersionForCompatibility();
							} else {
								String[] split = rooVersion.split("\\.");
								if (split.length > 2) {
									//only interested in major.minor
									rooVersion = split[0] + "." + split[1];
								}
							}
							
							BundleVersion version = new BundleVersion(versionElement.getAttribute("url"), versionElement.getAttribute("obr-url"), versionBuilder.toString(), versionElement.getAttribute("name"), new Long(versionElement.getAttribute("size")).longValue(), versionElement.getAttribute("description"), pgpKey, signedBy, rooVersion, commands);
							// For security reasons we ONLY accept httppgp:// add-on versions
							if (!version.getUri().startsWith("httppgp://")) {
								continue;
							}
							bundle.addVersion(version);
						}
						bundleCache.put(bsn, bundle);
					}
				}
				success = true;
			}
			zip.close();
			baos.close();
			bais.close();
		} catch (Throwable ignore) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ignored) {
			}
		}
		if (success && startupTime) {
			//printAddonStats();
		}
		return success;
	}


	private int countBundles() {
		BundleContext bc = context.getBundleContext();
		if (bc != null) {
			org.osgi.framework.Bundle[] bundles = bc.getBundles();
			if (bundles != null) {
				return bundles.length;
			}
		}
		return 0;
	}
	
	private boolean verifyRepository(String repoUrl) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			URL obrUrl = null;
			obrUrl = new URL(repoUrl);
			DocumentBuilder db = dbf.newDocumentBuilder();
			if (obrUrl.toExternalForm().endsWith(".zip")) {
				ZipInputStream zip = new ZipInputStream(obrUrl.openStream());
				zip.getNextEntry();
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[8192];
				int length = -1;
				while (zip.available() > 0) {
					length = zip.read(buffer, 0, 8192);
					if (length > 0) {
						baos.write(buffer, 0, length);
					}
				}
				ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
				doc = db.parse(bais);
			} else {
				doc = db.parse(obrUrl.openStream());
			}
			Assert.notNull(doc, "RooBot was unable to parse the repository document of this add-on");
			for (Element resource: XmlUtils.findElements("resource", doc.getDocumentElement())) {
				if (resource.hasAttribute("uri")) {
					if (!resource.getAttribute("uri").startsWith("httppgp")) {
						log.warning("Sorry, the resource " + resource.getAttribute("uri") + " does not follow HTTPPGP conventions mangraded by Spring Roo so the OBR file at " + repoUrl + " is unacceptable at this time");
						return false;
					}
				}
			}
			doc = null;
		} catch (Exception e) {
			throw new IllegalStateException("RooBot was unable to parse the repository document of this add-on", e);
		}
		return true;
	}
	
//	private AddOnStabilityLevel checkAddOnStabilityLevel(AddOnStabilityLevel addOnStabilityLevel) {
//		if (addOnStabilityLevel == null) {
//			addOnStabilityLevel = AddOnStabilityLevel.fromLevel(prefs.getInt(ADDON_UPGRADE_STABILITY_LEVEL, /* default */ AddOnStabilityLevel.RELEASE.getLevel()));
//		}
//		return addOnStabilityLevel;
//	}

	private boolean isCompatible(String version) {
		return version.equals(getVersionForCompatibility());
	}
	
	private String getVersionForCompatibility() {
		return UaaRegistrationService.SPRING_ROO.getMajorVersion() + "." + UaaRegistrationService.SPRING_ROO.getMinorVersion();
	}
	
}