/*
 * Copyright 2002-2006 the original author or authors.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.views.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConstructorArgumentNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.beans.ui.views.model.PropertyNode;
import org.springframework.ide.eclipse.beans.ui.views.model.RootNode;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Adapter for DND support in beans view.
 * 
 * @author Pierre-Antoine Grégoire
 * @author Torsten Juergeleit
 */
public class BeansViewDropAdapter extends ViewerDropAdapter {
	/*
	 * ----------------------------------------------- THE SOURCES TYPES
	 */
	public final static int UNKNOWN_SOURCE = 1 << 0;

	public final static int CONFIG_FILE_SOURCE = 1 << 1;

	public final static int JAVA_FILE_SOURCE = 1 << 2;

	public final static int PROJECT_SOURCE = 1 << 3;

	public final static int ALL_RESOURCES_SOURCES = (UNKNOWN_SOURCE
			| CONFIG_FILE_SOURCE | JAVA_FILE_SOURCE | PROJECT_SOURCE);

	public final static int PROJECT_NODE_SOURCE = 1 << 4;

	public final static int CONFIG_NODE_SOURCE = 1 << 5;

	public final static int CONFIGSET_NODE_SOURCE = 1 << 6;

	public final static int BEAN_NODE_SOURCE = 1 << 7;

	public final static int PROPERTY_NODE_SOURCE = 1 << 8;

	public final static int CONSTRUCTORARGUMENT_NODE_SOURCE = 1 << 9;

	public final static int ALL_NODES_SOURCES = (PROJECT_NODE_SOURCE
			| CONFIG_NODE_SOURCE | CONFIGSET_NODE_SOURCE | BEAN_NODE_SOURCE
			| PROPERTY_NODE_SOURCE | CONSTRUCTORARGUMENT_NODE_SOURCE);

	// All drops : necessary to clear the DROP flags
	public final static int ALL_SOURCES = (UNKNOWN_SOURCE | CONFIG_FILE_SOURCE
			| JAVA_FILE_SOURCE | PROJECT_SOURCE | PROJECT_NODE_SOURCE
			| CONFIG_NODE_SOURCE | CONFIGSET_NODE_SOURCE | BEAN_NODE_SOURCE
			| PROPERTY_NODE_SOURCE | CONSTRUCTORARGUMENT_NODE_SOURCE);

	/*
	 * ----------------------------------------------- THE TARGETS TYPES
	 */
	public final static int CONFIG_NODE_TARGET = 1 << 10;

	public final static int CONFIGSET_NODE_TARGET = 1 << 11;

	public final static int PROJECT_NODE_TARGET = 1 << 12;

	public final static int ROOT_NODE_TARGET = 1 << 13;

	public final static int BEAN_NODE_TARGET = 1 << 14;

	public final static int PROPERTY_NODE_TARGET = 1 << 15;

	public final static int CONSTRUCTORARGUMENT_NODE_TARGET = 1 << 16;

	// All drops : necessary to clear the NODES flags
	public final static int ALL_TARGETS = (CONFIG_NODE_TARGET
			| CONFIGSET_NODE_TARGET | PROJECT_NODE_TARGET | ROOT_NODE_TARGET
			| BEAN_NODE_TARGET | PROPERTY_NODE_TARGET | CONSTRUCTORARGUMENT_NODE_TARGET);

	/*
	 * ----------------------------------------------- LOCATION FLAGS
	 */
	public final static int LOCATION_AROUND = 1 << 17;

	public final static int LOCATION_ON = 1 << 18;

	public final static int ALL_LOCATIONS = (LOCATION_AROUND | LOCATION_ON);

	public BeansViewDropAdapter(TreeViewer viewer) {
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
		Object selectedObjects = BeansViewUtils.getSelectedObjects();
		if (selectedObjects != null) {
			DropStatus dropStatus = new DropStatus();
			dropStatus.flagSourceType(selectedObjects);
			dropStatus.flagTargetType(dropTarget);
			dropStatus.flagDropLocation(getCurrentLocation());
			// determineDropType(dropStatus);
			// THE FOLLOWING TREE tests in this order :
			// IS THERE ONLY ONE SOURCE/ONE TARGET/ONE LOCATION
			// SOURCE->TARGET->LOCATION
			if (dropStatus.hasOnlyOneFlag(ALL_SOURCES)
					&& dropStatus.hasOnlyOneFlag(ALL_LOCATIONS)
					&& dropStatus.hasOnlyOneFlag(ALL_TARGETS)) {

				if (dropStatus.hasAnyFlag(ALL_RESOURCES_SOURCES)) {
					IResource[] resources = (IResource[]) selectedObjects;
					if (dropStatus.hasFlag(JAVA_FILE_SOURCE
							| CONFIG_NODE_TARGET | LOCATION_ON)) {
						// TODO drop Java file onto config
					} else if (dropStatus.hasFlag(CONFIG_FILE_SOURCE)) {
						if (dropStatus.hasFlag(CONFIG_NODE_TARGET
								| LOCATION_AROUND)) {
							ProjectNode project = (ProjectNode)
									dropTarget.getParent();
							BeansViewUtils.addConfigs(resources,
									project.getProject());
							selectAndReveal(project, true);
						} else if (dropStatus.hasFlag(PROJECT_NODE_TARGET
								| LOCATION_ON)) {
							ProjectNode project = (ProjectNode) dropTarget;
							BeansViewUtils.addConfigs(resources,
									project.getProject());
							selectAndReveal(project, true);
						}
					} else if (dropStatus.hasFlag(PROJECT_SOURCE)) {
						if (dropStatus.hasFlag(ROOT_NODE_TARGET | LOCATION_ON)) {
							BeansViewUtils.addProjects(resources);
						} else if (dropStatus.hasFlag(PROJECT_NODE_TARGET
								| LOCATION_AROUND)) {
							BeansViewUtils.addProjects(resources);
						}
					}
				} else if (dropStatus.hasAnyFlag(ALL_NODES_SOURCES)) {
					INode[] nodes = (INode[]) selectedObjects;
					if (dropStatus.hasFlag(CONFIG_NODE_SOURCE
							| CONFIGSET_NODE_TARGET | LOCATION_ON)) {
						BeansViewUtils.addNodes(nodes,
								((ConfigSetNode) dropTarget).getConfigSet());
					}
				}
			}
		}
		return false;
	}

	private void selectAndReveal(INode node, boolean expand) {
		TreeViewer viewer = (TreeViewer) getViewer();
		viewer.setSelection(new StructuredSelection(node), true);
		if (expand) {
			viewer.expandToLevel(node, 1);
		}
	}

	/**
	 * Helper class whichs provides a bit mask.
	 */
	public class DropStatus {

		public final static int BIT_NUMBERS = 32;

		int flagTable;

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

		public boolean hasOnlyOneFlag(int mask) {
			boolean result = false;
			int testValue = flagTable & mask;
			testValue = testValue < 0 ? -(testValue) : testValue;
			if (testValue != 0) {
				for (int in = 0; in < 32; in++) {
					if (testValue == Math.pow(2, in)) {
						result = true;
						break;
					}
				}
			}
			return result;
		}

		public boolean hasAnyFlag(int mask) {
			return (flagTable & mask) > 0;
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

		public void flagSourceType(Object datas) {
			int result = 0;
			if (datas instanceof IResource[]) {
				IResource[] resources = (IResource[]) datas;
				// This test of resources' length should not be useful but is
				// there for consistency
				if (resources.length > 0) {
					boolean allResourcesfromSameProject = (BeansViewUtils
							.areResourcesFromTheSameProject(resources));
					// SHOULD NOT DROP MULTIPLE RESOURCE TYPES
					switch (BeansViewUtils.getResourcesCommonType(resources)) {
					case IResource.NONE:
						// resource is not known
						result |= UNKNOWN_SOURCE;
						break;
					case IResource.FOLDER:
						// TODO handle folders
						break;
					case IResource.PROJECT:
						result |= PROJECT_SOURCE;
						break;
					case IResource.FILE:
						if (allResourcesfromSameProject
								&& SpringCoreUtils.isSpringProject(resources[0]
										.getProject())) {
							IBeansProject project = BeansCorePlugin.getModel().
										 getProject(resources[0].getProject());
							if (BeansViewUtils
									.areAllResourcesCompilationUnits(resources)) {
								result |= BeansViewDropAdapter.JAVA_FILE_SOURCE;
							} else if (BeansViewUtils
									.areAllResourcesConfigFiles(resources,
											project)) {
								result |= BeansViewDropAdapter.CONFIG_FILE_SOURCE;
							}
						}
						break;
					default:
						result |= UNKNOWN_SOURCE;
						break;
					}
				} else {
					result |= UNKNOWN_SOURCE;
				}
			}

			if (datas instanceof INode[]) {
				INode[] nodes = (INode[]) datas;
				INode lastNode = null;
				for (int i = 0; i < nodes.length; i++) {
					if (lastNode != null
							&& !lastNode.getClass().equals(nodes[i].getClass())) {
						// SHOULD NOT DROP MULTIPLE RESOURCE TYPES
						result &= ~BeansViewDropAdapter.ALL_SOURCES;
						result |= BeansViewDropAdapter.UNKNOWN_SOURCE;
						break;
					}
					if (nodes[i] instanceof ProjectNode) {
						result |= BeansViewDropAdapter.PROJECT_NODE_SOURCE;
					} else if (nodes[i] instanceof ConfigSetNode) {
						result |= BeansViewDropAdapter.CONFIGSET_NODE_SOURCE;
					} else if (nodes[i] instanceof ConfigNode) {
						result |= BeansViewDropAdapter.CONFIG_NODE_SOURCE;
					} else if (nodes[i] instanceof BeanNode) {
						result |= BeansViewDropAdapter.BEAN_NODE_SOURCE;
					} else if (nodes[i] instanceof PropertyNode) {
						result |= BeansViewDropAdapter.PROPERTY_NODE_SOURCE;
					} else if (nodes[i] instanceof ConstructorArgumentNode) {
						result |= BeansViewDropAdapter.CONSTRUCTORARGUMENT_NODE_SOURCE;
					}
				}

			}
			this.removeFlags(BeansViewDropAdapter.ALL_SOURCES);// erasing
			// potential
			// previous
			// setting
			this.addFlag(result);
		}

		public void flagTargetType(INode dropTarget) {
			this.removeFlags(BeansViewDropAdapter.ALL_TARGETS);
			if (dropTarget instanceof RootNode) {
				this.addFlag(BeansViewDropAdapter.ROOT_NODE_TARGET);
			} else if (dropTarget instanceof ProjectNode) {
				this.addFlag(BeansViewDropAdapter.PROJECT_NODE_TARGET);
			} else if (dropTarget instanceof ConfigSetNode) {
				this.addFlag(BeansViewDropAdapter.CONFIGSET_NODE_TARGET);
			} else if (dropTarget instanceof ConfigNode) {
				this.addFlag(BeansViewDropAdapter.CONFIG_NODE_TARGET);
			} else if (dropTarget instanceof BeanNode) {
				this.addFlag(BeansViewDropAdapter.BEAN_NODE_TARGET);
			} else if (dropTarget instanceof PropertyNode) {
				this.addFlag(BeansViewDropAdapter.PROPERTY_NODE_TARGET);
			} else if (dropTarget instanceof ConstructorArgumentNode) {
				this.addFlag(BeansViewDropAdapter.CONSTRUCTORARGUMENT_NODE_TARGET);
			}
		}

		public void flagDropLocation(int treeViewerCurrentLocation) {
			if (treeViewerCurrentLocation == ViewerDropAdapter.LOCATION_NONE) {
				this.addFlag(BeansViewDropAdapter.LOCATION_ON);
			} else if (treeViewerCurrentLocation == ViewerDropAdapter.LOCATION_ON) {
				this.addFlag(BeansViewDropAdapter.LOCATION_ON);
			} else if (treeViewerCurrentLocation == ViewerDropAdapter.LOCATION_AFTER
					|| treeViewerCurrentLocation == ViewerDropAdapter.LOCATION_BEFORE) {
				this.addFlag(BeansViewDropAdapter.LOCATION_AROUND);
			}
		}

		public String toString() {
			StringBuffer result = new StringBuffer();
			for (int i = BIT_NUMBERS - 1; i >= 0; i--) {
				result.append(Integer.toString((flagTable >> i) & 1));
			}
			return result.toString();
		}
	}
}
