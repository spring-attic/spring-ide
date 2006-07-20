/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.core.internal.document.CommentImpl;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.core.BeansTags;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.actions.LexicalSortingAction;
import org.springframework.ide.eclipse.beans.ui.editor.actions.OutlineStyleAction;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeansContentOutlineConfiguration
									   extends XMLContentOutlineConfiguration {
	private boolean showAttributes;

	/**
	 * Returns the bean editor plugin's preference store.
	 */
	protected IPreferenceStore getPreferenceStore() {
		return BeansEditorPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Adds the outline style toggle to the context menu.
	 */
	protected IContributionItem[] createMenuContributions(TreeViewer viewer) {
		IContributionItem styleItem = new ActionContributionItem(
				new OutlineStyleAction(viewer));
		IContributionItem[] items = super.createMenuContributions(viewer);
		if (items == null) {
			items = new IContributionItem[] { styleItem };
		} else {
			IContributionItem[] combinedItems = new IContributionItem[
			                                                 items.length + 1];
			System.arraycopy(items, 0, combinedItems, 0, items.length);
			combinedItems[items.length] = styleItem;
			items = combinedItems;
		}
		return items;
	}

	/**
	 * Adds the sort toggle to the toolbar.
	 */
	protected IContributionItem[] createToolbarContributions(TreeViewer viewer) {
		IContributionItem sortItem = new ActionContributionItem(
											 new LexicalSortingAction(viewer));
		IContributionItem[] items = super.createToolbarContributions(viewer);
		if (items == null) {
			items = new IContributionItem[] { sortItem };
		} else {
			IContributionItem[] combinedItems = new IContributionItem[
			                                                 items.length + 1];
			System.arraycopy(items, 0, combinedItems, 0, items.length);
			combinedItems[items.length] = sortItem;
			items = combinedItems;
		}
		return items;
	}

	protected void enableShowAttributes(boolean showAttributes, TreeViewer treeViewer) {
		this.showAttributes = showAttributes;
	}

	/**
	 * Returns the wrapped original XML outline content provider which is only
	 * used if the outline view is non-spring style. This way the XML outline's
	 * "Show Attributes" feature doesn't interfer with a non-spring style
	 * outline view.
	 * @see BeansOutlineLabelProvider
	 */
	public ILabelProvider getLabelProvider(TreeViewer viewer) {
		return new BeansOutlineLabelProvider(super.getLabelProvider(viewer));
	}

	private final class BeansOutlineLabelProvider extends JFaceNodeLabelProvider {

		private ILabelProvider xmlProvider;

		public BeansOutlineLabelProvider(ILabelProvider xmlProvider) {
			this.xmlProvider = xmlProvider;
		}

		public Image getImage(Object object) {
			if (!BeansEditorUtils.isSpringStyleOutline()) {
				return xmlProvider.getImage(object);
			}

			// Create Spring beans label image
			Node node = (Node) object;
			String nodeName = node.getNodeName();
			NamedNodeMap attributes = node.getAttributes();
	
			// Root elements (alias, import and bean)
			if ("alias".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_ALIAS);
			}
			if ("import".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_IMPORT);
			}
			if ("bean".equals(nodeName)) {
				int flags = 0;
				if (attributes.getNamedItem("parent") != null) {
					flags |= BeansModelImages.FLAG_CHILD;
				} else if (attributes.getNamedItem("factory-method") != null) {
					flags |= BeansModelImages.FLAG_FACTORY;
				}
				return BeansModelImages.getImage(BeansModelImages.ELEMENT_BEAN,
												 flags);
			}

			// Bean elements
			if ("constructor-arg".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTRUCTOR);
			}
			if ("property".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY);
			}
	
			// Misc elements
			if ("list".equals(nodeName) || "set".equals(nodeName) ||
						  "map".equals(nodeName) || "props".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_COLLECTION);
			}
			if ("ref".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN_REF);
			}
			if ("description".equals(nodeName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_DESCRIPTION);
			}
			return xmlProvider.getImage(object);
		}

		public String getText(Object o) {
			if (!BeansEditorUtils.isSpringStyleOutline()) {
				return xmlProvider.getText(o);
			}

			// Create Spring beans label text
			Node node = (Node) o;
			NamedNodeMap attrs = node.getAttributes(); 
			Node attr;
			String text = "";

			// Root elements (alias, import and bean)
			switch (BeansTags.getTag(node)) {
				case BeansTags.IMPORT :
					attr = attrs.getNamedItem("resource");
					if (attr != null) {
						text = attr.getNodeValue();
					}
					break;

				case BeansTags.ALIAS :
					attr = attrs.getNamedItem("name");
					if (attr != null) {
						text = attr.getNodeValue();
					}
					if (showAttributes) {
						attr = attrs.getNamedItem("alias");
						if (attr != null) {
							text += " \"" + attr.getNodeValue() + "\"";
						}
					}
					break;

				case BeansTags.BEAN :
					boolean hasParent = false;
					attr = attrs.getNamedItem("id");
					if (attr != null) {
						text = attr.getNodeValue();
					} else {
						attr = attrs.getNamedItem("name");
						if (attr != null) {
							text = attr.getNodeValue();
						} else {
							attr = attrs.getNamedItem("parent");
							if (attr != null) {
								text = "<" + attr.getNodeValue() + ">";
								hasParent = true;
							}
						}
					}
					if (showAttributes) {
						attr = attrs.getNamedItem("class");
						if (attr != null) {
							if (text.length() > 0) {
								text += ' ';
							}
							text += '[' + attr.getNodeValue() + ']';
						}
						if (!hasParent) {
							attr = attrs.getNamedItem("parent");
							if (attr != null) {
								if (text.length() > 0) {
									text += ' ';
								}
								text += '<' + attr.getNodeValue() + '>';
							}
						}
					}
					break;

				case BeansTags.CONSTRUCTOR_ARG :
					attr = attrs.getNamedItem("index");
					if (attr != null) {
						text += " {" + attr.getNodeValue() + "}";
					}
					attr = attrs.getNamedItem("type");
					if (attr != null) {
						text += " [" + attr.getNodeValue() + "]";
					}
					attr = attrs.getNamedItem("ref");
					if (attr != null) {
						text += " <" + attr.getNodeValue() + ">";
					}
					attr = attrs.getNamedItem("value");
					if (attr != null) {
						text += " \"" + attr.getNodeValue() + "\"";
					}
					break;

				case BeansTags.PROPERTY :
					attr = attrs.getNamedItem("name");
					if (attr != null) {
						text = attr.getNodeValue();
					}
					if (showAttributes) {
						attr = attrs.getNamedItem("ref");
						if (attr != null) {
							text += " <" + attr.getNodeValue() + ">";
						}
						attr = attrs.getNamedItem("value");
						if (attr != null) {
							text += " \"" + attr.getNodeValue() + "\"";
						}
					}
					break;

				case BeansTags.REF :
				case BeansTags.IDREF :
					attr = attrs.getNamedItem("bean");
					if (attr != null) {
						text += "<" + attr.getNodeValue() + ">";
					}
					attr = attrs.getNamedItem("local");
					if (attr != null) {
						text += "<" + attr.getNodeValue() + ">";
					}
					attr = attrs.getNamedItem("parent");
					if (attr != null) {
						text += "<" + attr.getNodeValue() + ">";
					}
					break;

				case BeansTags.VALUE :
					text = node.getNodeName();
					if (showAttributes) {
						attr = attrs.getNamedItem("type");
						if (attr != null) {
							text += " [" + attr.getNodeValue() + "]";
						}
					}
					break;

				case BeansTags.ENTRY :
					text = node.getNodeName();
					attr = attrs.getNamedItem("key");
					if (attr != null) {
						text += " \"" + attr.getNodeValue() + "\"";
					} else {
						attr = attrs.getNamedItem("key-ref");
						if (attr != null) {
							text += " <" + attr.getNodeValue() + ">";
						}
					}
					if (showAttributes) {
						attr = attrs.getNamedItem("value");
						if (attr != null) {
							text += " \"" + attr.getNodeValue() + "\"";
						}
					}
					break;

				case BeansTags.PROP :
					text = node.getNodeName();
					attr = node.getFirstChild();
					if (attr != null && attr.getNodeType() == Node.TEXT_NODE) {
						text += " \"" + attr.getNodeValue() + "\"";
					}
					break;
					
				case BeansTags.COMMENT :
					text = super.getText(o);
					text += " <";
					text += ((CommentImpl) o).getNodeValue().trim();
					text += '>';
					break;
					
				default :
					text = super.getText(o);
			}
			return text;
		}
	}
}
