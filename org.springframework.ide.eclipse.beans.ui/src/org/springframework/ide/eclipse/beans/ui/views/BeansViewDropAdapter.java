/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.views;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUILabelDecorator;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;
import org.springframework.ide.eclipse.beans.ui.model.ProjectNode;
import org.springframework.ide.eclipse.beans.ui.model.RootNode;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Adapter for DND support in beans view.
 * 
 * @author Pierre-Antoine Grégoire
 */
public class BeansViewDropAdapter extends ViewerDropAdapter {

	/*
	 * ----------------------------------------------- THE DROP TYPES
	 */

	// No drop is to be done
	private final static int NO_DROP = 1 << 0;

	//  A configuration drop is to be done
	private final static int XML_DROP_ON_CONFIG = 1 << 1;

	//  A configuration drop is to be done at the parent's level
	private final static int XML_DROP_ON_PROJECT = 1 << 2;

	//  A bean drop is to be done
	private final static int JAVATYPE_DROP_ON_CONFIG = 1 << 3;

	//  A project drop is to be done
	private final static int PROJECT_DROP = 1 << 4;

	//  A project drop is to be done at the root's level
	private final static int PROJECT_ROOT_DROP = 1 << 5;

	// All drops : necessary to clear the DROP flags
	private final static int ALL_DROPS = (NO_DROP | XML_DROP_ON_CONFIG
			| XML_DROP_ON_PROJECT | JAVATYPE_DROP_ON_CONFIG | PROJECT_DROP | PROJECT_ROOT_DROP);

	/*
	 * ----------------------------------------------- THE DROPPED DATAs TYPES
	 */
	private final static int UNKNOWN_DROPPED_DATA = 1 << 6;

	private final static int CONFIG_DROPPED_DATA = 1 << 7;

	private final static int BEAN_DROPPED_DATA = 1 << 8;

	private final static int PROJECT_DROPPED_DATA = 1 << 9;

	//  All drops : necessary to clear the DROP flags
	private final static int ALL_DROPPED_DATAS = (UNKNOWN_DROPPED_DATA
			| CONFIG_DROPPED_DATA | BEAN_DROPPED_DATA | PROJECT_DROPPED_DATA);

	/*
	 * ----------------------------------------------- THE DROP TARGETS
	 */
	private final static int CONFIG_NODE = 1 << 10;

	private final static int CONFIGSET_NODE = 1 << 11;

	private final static int PROJECT_NODE = 1 << 12;

	private final static int ROOT_NODE = 1 << 13;

	private final static int BEAN_NODE = 1 << 14;

	//  All drops : necessary to clear the NODES flags
	private final static int ALL_NODES = (CONFIG_NODE | CONFIGSET_NODE
			| PROJECT_NODE | ROOT_NODE | BEAN_NODE);

	/*
	 * ----------------------------------------------- REUSE FLAGS
	 */
	private int lastObjectHash = 0;
	private int lastObjectDroppedData = UNKNOWN_DROPPED_DATA;

	public BeansViewDropAdapter(Viewer viewer) {
		super(viewer);
		setScrollExpandEnabled(true);
	}

	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		return LocalSelectionTransfer.getInstance().isSupportedType(
				transferType);
	}

	public boolean performDrop(Object data) {
		INode dropTarget = (INode) getCurrentTarget();
		if (dropTarget == null) {
			dropTarget = (INode) getViewer().getInput();
		}
		IResource[] resources = getSelectedResources();
		DropStatus dropStatus = new DropStatus();
		flagDroppedData(resources, dropStatus);
		flagNodeType(dropTarget, dropStatus);
		flagDropType(dropStatus);

		switch (dropStatus.getFlagTable() & ~ALL_DROPPED_DATAS
				& ~ALL_NODES) {
//		case JAVATYPE_DROP_ON_CONFIG:
//			performBeanDrop(resources, dropTarget, dropStatus);
//			return true;

		case XML_DROP_ON_PROJECT:
			performConfigDrop(resources, (ProjectNode) dropTarget,
					dropStatus);
			return true;

		case XML_DROP_ON_CONFIG:
			performConfigDrop(resources, (ProjectNode) dropTarget.getParent(),
					dropStatus);
			return true;

//		case PROJECT_DROP:
//			performProjectDrop(resources, dropTarget, dropStatus);
//			return true;

//		case PROJECT_ROOT_DROP:
//			performProjectDrop(resources, dropTarget.getParent(),
//					dropStatus);
		}
		return false;
	}

    /**
     * Returns the resource selection from the LocalSelectionTransfer.
     * 
     * @return the resource selection from the LocalSelectionTransfer
     */
    private IResource[] getSelectedResources() {
        ArrayList selectedResources = new ArrayList();

        ISelection selection = LocalSelectionTransfer.getInstance()
                .getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            for (Iterator i = ssel.iterator(); i.hasNext();) {
                Object o = i.next();
                if (o instanceof IResource) {
                    selectedResources.add(o);
                }
                else if (o instanceof IAdaptable) {
                    IAdaptable a = (IAdaptable) o;
                    IResource r = (IResource) a.getAdapter(IResource.class);
                    if (r != null) {
                        selectedResources.add(r);
                    }
                }
            }
        }
		return (IResource[]) selectedResources
				.toArray(new IResource[selectedResources.size()]);
    }

	private void flagDroppedData(IResource[] resources, DropStatus dropMask) {
		int result = 0;
		if (lastObjectHash == resources.hashCode()) {
			result = lastObjectDroppedData;
		} else {
			lastObjectHash = resources.hashCode();
			int resourceType = IResource.NONE;
			String projectId = null;
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resourceType != IResource.NONE
						&& resource.getType() != resourceType) {
					// SHOULD NOT DROP MULTIPLE RESOURCE TYPES
					result |= UNKNOWN_DROPPED_DATA;
					break;
				} else if (projectId != null
						&& !(resource.getProject().getName().equals(projectId))) {
					// SHOULD NOT DROP RESOURCES FROM SEPARATE PROJECTS.
					result |= UNKNOWN_DROPPED_DATA;
					break;
				} else if (resource.getType() == IResource.FOLDER) {
					resourceType = resource.getType();
					break;
				} else {
					resourceType = resource.getType();
					projectId = resource.getProject().getName();
					if (resourceType == IResource.FILE) {
						if (SpringCoreUtils
								.isJavaProject(resource.getProject())) {
							if (resource.getAdapter(ICompilationUnit.class) != null) {
								result |= BEAN_DROPPED_DATA;
							} else if (resource.getName().toLowerCase().trim()
									.endsWith(".xml")) {
								result |= CONFIG_DROPPED_DATA;
							}
						}
					}
				}
			}
			switch (resourceType) {
			case IResource.PROJECT:
				result |= PROJECT_DROPPED_DATA;
				break;

			case IResource.FOLDER:
				result |= UNKNOWN_DROPPED_DATA;
				break;

			default:
				break;
			}
		}
		lastObjectDroppedData = result;
		dropMask.removeFlags(ALL_DROPPED_DATAS);// erasing potential previous
												// setting
		dropMask.addFlag(result);
	}

	private void flagDropType(DropStatus dropStatus) {
		int result = 0;	//erasing potential previous setting
		int currentLocation = getCurrentLocation();
		if (!dropStatus.hasFlag(UNKNOWN_DROPPED_DATA)) {

			// TARGET is a configNode and SOURCE is not a project
			if (dropStatus.hasFlag(CONFIG_NODE)
					&& !dropStatus.hasFlag(PROJECT_DROPPED_DATA)) {
				if (currentLocation != LOCATION_NONE) {
					if (currentLocation == LOCATION_ON) {
						if (dropStatus.hasFlag(CONFIG_DROPPED_DATA)) {
							result = XML_DROP_ON_CONFIG;
						} else {
							result = JAVATYPE_DROP_ON_CONFIG;
						}
					} else if (currentLocation == LOCATION_BEFORE
							|| getCurrentLocation() == LOCATION_AFTER) {
						result = XML_DROP_ON_PROJECT;
					}
				}
			}

			// TARGET is a beanNode and SOURCE is not a project nor a config
			else if (dropStatus.hasFlag(BEAN_NODE)
					&& !dropStatus.hasFlag(CONFIG_DROPPED_DATA)
					&& !dropStatus.hasFlag(PROJECT_DROPPED_DATA)) {
				if (currentLocation != LOCATION_NONE) {
					if (currentLocation == LOCATION_ON) {
						result = NO_DROP;// nothing dropped on beans (for now...)
					} else if (currentLocation == LOCATION_BEFORE
							|| currentLocation == LOCATION_AFTER) {
						result = JAVATYPE_DROP_ON_CONFIG;
					}
				}
			}

			// TARGET is a projectNode and SOURCE is not a bean
			else if (dropStatus.hasFlag(PROJECT_NODE)
					&& !dropStatus.hasFlag(BEAN_DROPPED_DATA)) {
				if (currentLocation != LOCATION_NONE) {
					if (currentLocation == LOCATION_ON) {
						if (dropStatus.hasFlag(CONFIG_DROPPED_DATA)) {
							result = XML_DROP_ON_PROJECT;
						} else if (dropStatus.hasFlag(PROJECT_DROPPED_DATA)) {
							result = PROJECT_ROOT_DROP;
						}
					} else if (currentLocation == LOCATION_BEFORE
							|| currentLocation == LOCATION_AFTER) {
						result = PROJECT_ROOT_DROP;
					}
				}
			} else {
				result = PROJECT_DROP;
			}
		} else {
			result = NO_DROP;
		}
		dropStatus.removeFlags(ALL_DROPS);
		dropStatus.addFlag(result);
	}

	private void flagNodeType(INode dropTarget, DropStatus dropStatus) {
		dropStatus.removeFlags(ALL_NODES);
		if (dropTarget instanceof ConfigNode) {
			dropStatus.addFlag(CONFIG_NODE);
		} else if (dropTarget instanceof BeanNode) {
			dropStatus.addFlag(BEAN_NODE);
		} else if (dropTarget instanceof ProjectNode) {
			dropStatus.addFlag(PROJECT_NODE);
		} else if (dropTarget instanceof ConfigSetNode) {
			dropStatus.addFlag(CONFIGSET_NODE);
		} else if (dropTarget instanceof RootNode) {
			dropStatus.addFlag(ROOT_NODE);
		}
	}

//	private void performBeanDrop(IResource[] resources, ConfigNode config,
//			DropStatus dropMask) {
//		for (int i = 0; i < resources.length; i++) {
//			BeanNode bean = new BeanNode(config, resources[i].getName());
//			bean.setParent(config);
//			config.addBean(bean);
//			((TreeViewer) getViewer()).add(config, bean);
//			((TreeViewer) getViewer()).reveal(bean);
//		}
//	}

	private void performConfigDrop(IResource[] resources, ProjectNode project,
			DropStatus dropMask) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			String name = resource.getProjectRelativePath().toString();
			BeansProject beansProject = (BeansProject) BeansCorePlugin
					.getModel().getProject(resource.getProject());
			if (!beansProject.hasConfig(name)) {
				ConfigNode config = new ConfigNode(project, name);
				config.setParent(project);
				project.addConfig(config.getName());
				beansProject.setConfigs(project.getConfigNames());
				BeansUILabelDecorator.update();
				((TreeViewer) getViewer()).add(project, config);
				((TreeViewer) getViewer()).reveal(config);
			}
		}
	}

//	private void performProjectDrop(IResource[] resources, RootNode root,
//			DropStatus dropMask) {
//		for (int i = 0; i < resources.length; i++) {
//			IResource resource = (IResource) resources[i];
//			IProject project = resource.getProject();
//			if (!SpringCoreUtils.isSpringProject(project)) {
//				SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID);
//				ProjectNode projectNode = new ProjectNode(root, project
//						.getName());
//				projectNode.setParent(root);
//				root.addProject(projectNode.getName(), new ArrayList(),
//						new ArrayList());
//				BeansUILabelDecorator.update();
//				((TreeViewer) getViewer()).add(root, projectNode);
//				((TreeViewer) getViewer()).reveal(projectNode);
//			}
//		}
//	}

	/**
	 * Helper class whichs provides a bit mask.
	 */
	public class DropStatus {

		public final static int BIT_NUMBERS = 32;

		private int flagTable;

		public DropStatus(int initValue) {
			flagTable = initValue;
		}

		public DropStatus() {
			flagTable = 0;
		}

		public void addFlag(int mask) {
			flagTable |= mask;
		}

		public boolean hasFlag(int mask) {
			return (flagTable & mask) == mask;
		}

		public int getFlagTable() {
			return flagTable;
		}

		public void setFlagTable(int flagTable) {
			this.flagTable = flagTable;
		}

		public void removeFlags(int mask) {
			flagTable &= ~mask;
		}

		public String toString() {
			StringBuffer result = new StringBuffer();
			for (int i = BIT_NUMBERS; i >= 0; i--) {
				result.append(Integer.toString((flagTable >> i) & 1));
			}
			return result.toString();
		}
	}
}
