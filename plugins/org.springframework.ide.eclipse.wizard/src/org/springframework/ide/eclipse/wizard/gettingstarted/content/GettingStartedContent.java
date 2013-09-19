/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.wizard.gettingstarted.github.GithubClient;
import org.springframework.ide.eclipse.wizard.gettingstarted.github.Repo;
import org.springframework.ide.eclipse.wizard.gettingstarted.util.DownloadManager;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

/**
 * Singleton class. The instance of this class provides access to all the
 * getting started content.
 *
 * NOTE: templates are not (yet?) included in this. Code to discover and
 * manage them already existed before this framework was implemented.
 */
public class GettingStartedContent extends ContentManager {

	private static GettingStartedContent INSTANCE = null;

	private final static boolean ADD_REAL =  true;
	private final static boolean ADD_MOCKS = false; // (""+Platform.getLocation()).contains("kdvolder")

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder")
				|| (""+Platform.getLocation()).contains("bamboo");

	public static GettingStartedContent getInstance() {
		//TODO: propagate progress monitor. Make sure this isn't called in UIThread. It may
		// hang downloading properties if user has no or poor internet access.
		if (INSTANCE == null) {
			INSTANCE = new GettingStartedContent(StsProperties.getInstance(new NullProgressMonitor()));
		}
		return INSTANCE;
	}

	private final GithubClient github = new GithubClient();


	/**
	 * We need this in multiple places. So cache it to avoid asking for it multiple times in a row.
	 */
	private Repo[] cachedRepos = null;

	private Repo[] getGuidesRepos() {
		if (cachedRepos==null) {
			Repo[] repos = github.getOrgRepos("spring-guides");
			if (DEBUG) {
				Arrays.sort(repos, new Comparator<Repo>() {
					public int compare(Repo o1, Repo o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				System.out.println("==== spring-guides-repos ====");
				int count = 1;
				for (Repo r : repos) {
					System.out.println(count++ + ":" + r.getName());
				}
				System.out.println("==== spring-guides-repos ====");
			}
			cachedRepos = repos;
		}
		return cachedRepos;
	}

	private GettingStartedContent(final StsProperties stsProps) {
		//Guides: are discoverable because they are all repos in org on github
		register(GettingStartedGuide.class, GettingStartedGuide.GUIDE_DESCRIPTION_TEXT,
			new ContentProvider<GettingStartedGuide>() {
//				@Override
				public GettingStartedGuide[] fetch(DownloadManager downloader) {
					LinkedHashMap<String, GettingStartedGuide> guides = new LinkedHashMap<String, GettingStartedGuide>();
					if (ADD_MOCKS) {
						addGuidesFrom(github.getMyRepos(), guides, downloader);
					}
					if (ADD_REAL) {
						addGuidesFrom(getGuidesRepos(), guides, downloader);
					}
					return guides.values().toArray(new GettingStartedGuide[guides.size()]);
				}

				private LinkedHashMap<String, GettingStartedGuide> addGuidesFrom(Repo[] repos, LinkedHashMap<String, GettingStartedGuide> guides, DownloadManager downloader) {
					for (Repo repo : repos) {
						String name = repo.getName();
	//					System.out.println("repo : "+name + " "+repo.getUrl());
						if (name.startsWith("gs-") && !guides.containsKey(name)) {
							guides.put(name, new GettingStartedGuide(stsProps, repo, downloader));
						}
					}
					return guides;
				}
			}
		);

		register(TutorialGuide.class, TutorialGuide.GUIDE_DESCRIPTION_TEXT,
			new ContentProvider<TutorialGuide>() {
//				@Override
				public TutorialGuide[] fetch(DownloadManager downloader) {
					LinkedHashMap<String, TutorialGuide> guides = new LinkedHashMap<String, TutorialGuide>();
					addGuidesFrom(getGuidesRepos(), guides, downloader);
					return guides.values().toArray(new TutorialGuide[guides.size()]);
				}

				private LinkedHashMap<String, TutorialGuide> addGuidesFrom(Repo[] repos, LinkedHashMap<String, TutorialGuide> guides, DownloadManager downloader) {
					for (Repo repo : repos) {
						String name = repo.getName();
	//					System.out.println("repo : "+name + " "+repo.getUrl());
						if (name.startsWith("tut-") && !guides.containsKey(name)) {
							guides.put(name, new TutorialGuide(stsProps, repo, downloader));
						}
					}
					return guides;
				}
			}
		);


		//References apps: are discoverable because we maintain a list of json metadata
		//that can be downloaded from some external url.
		register(ReferenceApp.class, ReferenceApp.REFERENCE_APP_DESCRIPTION,
			new ContentProvider<ReferenceApp>() {

//			@Override
			public ReferenceApp[] fetch(DownloadManager downloader) {
				ReferenceAppMetaData[] infos = github.get("https://raw.github.com/kdvolder/spring-reference-apps-meta/master/reference-apps.json", ReferenceAppMetaData[].class);
				ReferenceApp[] apps = new ReferenceApp[infos.length];
				for (int i = 0; i < apps.length; i++) {
					//TODO: it could be quite costly to create all these since each one
					// entails a request to obtain info about github repo.
					// Maybe this is a good reason to put a bit more info in the
					// json metadata and thereby avoid querying github to fetch it.
					apps[i] = create(downloader, infos[i]);
				}
				return apps;
			}

			private ReferenceApp create(DownloadManager dl, ReferenceAppMetaData md) {
				return new ReferenceApp(md, dl, github);
			}

		});
	}



	/**
	 * Get all getting started guides.
	 */
	public GettingStartedGuide[] getGSGuides() {
		return get(GettingStartedGuide.class);
	}

	/**
	 * Get all tutorial guides
	 */
	public TutorialGuide[] getTutorials() {
		return get(TutorialGuide.class);
	}

	public ReferenceApp[] getReferenceApps() {
		return get(ReferenceApp.class);
	}

	/**
	 * Get all guide content (i.e. tutorials + gs)
	 */
	public GithubRepoContent[] getAllGuides() {
		ArrayList<GithubRepoContent> all = new ArrayList<GithubRepoContent>();
		all.addAll(Arrays.asList(getTutorials()));
		all.addAll(Arrays.asList(getGSGuides()));
		return all.toArray(new GithubRepoContent[all.size()]);
	}

//	public GettingStartedGuide getGuide(String guideName) {
//		GettingStartedGuide[] guides = getGuides();
//		if (guides!=null) {
//			for (GettingStartedGuide g : guides) {
//				if (guideName.equals(g.getName())) {
//					return g;
//				}
//			}
//		}
//		return null;
//	}

}
